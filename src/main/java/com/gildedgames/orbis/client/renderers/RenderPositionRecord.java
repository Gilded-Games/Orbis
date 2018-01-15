package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IPositionRecord;
import com.gildedgames.orbis.api.data.schedules.IPositionRecordListener;
import com.gildedgames.orbis.api.util.mc.BlockUtil;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.util.BlockRenderUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderPositionRecord implements IWorldRenderer, IPositionRecordListener<BlockFilter>
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final BlockRenderUtil blockRenderer = new BlockRenderUtil(
			ObfuscationReflectionHelper.getPrivateValue(BlockModelRenderer.class, mc.getBlockRendererDispatcher().getBlockModelRenderer(), 0));

	private final IPositionRecord<BlockFilter> positionRecord;

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Map<BlockFilter, GlIndexCache> filterToGlIndexCache = Maps.newHashMap();

	private final Random rand = new Random();

	private final IWorldObject parentObject;

	private final SingleStateAccess stateAccess = new SingleStateAccess(null, this.mc.world, null);

	private final List<BlockFilter> pendingFiltersToCache = Lists.newArrayList();

	private boolean disabled;

	private Iterable<BlockPos.MutableBlockPos> shapeData;

	private BlockPos lastPos;

	public RenderPositionRecord(final IPositionRecord positionRecord, final IWorldObject parentObject)
	{
		this.positionRecord = positionRecord;
		this.parentObject = parentObject;

		this.positionRecord.listen(this);

		this.pendingFiltersToCache.addAll(Arrays.asList(this.positionRecord.getData()));
	}

	protected void bindTexture(final ResourceLocation location)
	{
		final TextureManager texturemanager = TileEntityRendererDispatcher.instance.renderEngine;

		if (texturemanager != null)
		{
			texturemanager.bindTexture(location);
		}
	}

	private void cacheRenderData(final BlockFilter filter)
	{
		this.lastPos = this.parentObject.getPos();

		if (this.shapeData == null)
		{
			this.shapeData = this.positionRecord.getBoundingBox().createShapeData();
		}

		final GlIndexCache cache = new GlIndexCache(this.positionRecord.getWidth(), this.positionRecord.getHeight(), this.positionRecord.getLength());

		this.filterToGlIndexCache.put(filter, cache);

		GlStateManager.pushMatrix();

		for (final BlockPos pos : this.shapeData)
		{
			this.renderPos(filter, pos, cache);
		}

		GlStateManager.popMatrix();
	}

	private void renderPos(final BlockFilter filter, final BlockPos renderPos, final GlIndexCache cache)
	{
		final IBlockState state = filter.getSample(this.mc.world, this.rand, Blocks.AIR.getDefaultState());

		if (state != null && !BlockUtil.isAir(state) && !BlockUtil.isVoid(state) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
		{
			GlStateManager.pushMatrix();

			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();

			final int x = renderPos.getX();
			final int y = renderPos.getY();
			final int z = renderPos.getZ();

			final int glIndex = cache.markPos(x, y, z, GLAllocation.generateDisplayLists(1));
			GlStateManager.glNewList(glIndex, GL11.GL_COMPILE);

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			RenderHelper.disableStandardItemLighting();

			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			buffer.setTranslation(this.lastPos.getX(), this.lastPos.getY(), this.lastPos.getZ());

			GlStateManager.enableCull();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

			if (Minecraft.isAmbientOcclusionEnabled())
			{
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
			}
			else
			{
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}

			final IBakedModel modelBaked = mc.getBlockRendererDispatcher().getModelForState(state);

			this.stateAccess.setPos(renderPos);
			this.stateAccess.setState(state);

			blockRenderer.renderModel(this.stateAccess, modelBaked, state, renderPos, buffer, true, MathHelper.getPositionRandom(renderPos));

			buffer.setTranslation(0, 0, 0);

			tessellator.draw();

			RenderHelper.enableStandardItemLighting();

			GlStateManager.glEndList();

			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabled;
	}

	@Override
	public void setDisabled(final boolean disabled)
	{
		this.disabled = disabled;
	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return null;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.parentObject.getShape().getBoundingBox();
	}

	@Override
	public void render(final World world, final float partialTicks)
	{
		for (final BlockFilter filter : this.pendingFiltersToCache)
		{
			this.cacheRenderData(filter);
		}

		this.pendingFiltersToCache.clear();

		if (this.lastPos == null)
		{
			this.lastPos = this.parentObject.getPos();
		}

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (!this.lastPos.equals(this.parentObject.getPos()))
		{
			GlStateManager.translate(this.parentObject.getPos().getX() - this.lastPos.getX(),
					this.parentObject.getPos().getY() - this.lastPos.getY(),
					this.parentObject.getPos().getZ() - this.lastPos.getZ());
		}

		GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);

		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();

		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < this.positionRecord.getVolume(); i++)
		{
			if (this.positionRecord.contains(i))
			{
				final BlockFilter filter = this.positionRecord.get(i);

				if (this.filterToGlIndexCache.containsKey(filter))
				{
					final int glIndex = this.filterToGlIndexCache.get(filter).glIndexes[i];

					if (glIndex != -1)
					{
						GlStateManager.callList(glIndex);

						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					}
				}
			}
		}

		GlStateManager.translate(0, 0, 0);

		GlStateManager.resetColor();

		GlStateManager.popMatrix();
	}

	@Override
	public List<IWorldRenderer> getSubRenderers(final World world)
	{
		return this.subRenderers;
	}

	@Override
	public ReadWriteLock getSubRenderersLock()
	{
		return this.lock;
	}

	@Override
	public void onRemoved()
	{
		for (final GlIndexCache cache : this.filterToGlIndexCache.values())
		{
			for (int i = 0; i < this.positionRecord.getVolume(); i++)
			{
				final int glIndex = cache.glIndexes[i];

				if (glIndex != -1)
				{
					GLAllocation.deleteDisplayLists(glIndex, 1);
				}
			}

			Arrays.fill(cache.glIndexes, -1);
		}
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Override
	public void onMarkPos(final BlockFilter filter, final int x, final int y, final int z)
	{
		if (!this.filterToGlIndexCache.containsKey(filter))
		{
			this.pendingFiltersToCache.add(filter);
		}
	}

	@Override
	public void onUnmarkPos(final int x, final int y, final int z)
	{

	}

	public class GlIndexCache
	{
		private final int width;

		private final int height;

		private final int length;

		protected int[] glIndexes;

		private int volume;

		public GlIndexCache(final int width, final int height, final int length)
		{
			this.width = width;
			this.height = height;
			this.length = length;

			this.createMarkedPositions();
		}

		private void createMarkedPositions()
		{
			this.volume = this.width * this.height * this.length;

			this.glIndexes = new int[this.getVolume()];
		}

		public int getVolume()
		{
			return this.volume;
		}

		private int getIndex(final int x, final int y, final int z)
		{
			final int index = z + y * this.length + x * this.height * this.length;

			if (index < this.getVolume() && index >= 0)
			{
				return index;
			}

			throw new ArrayIndexOutOfBoundsException("Tried to access position that's not in this GlIndexCache: " + x + ", " + y + ", " + z);
		}

		public int getZ(final int index)
		{
			return index / (this.width * this.length);
		}

		public int getY(int index)
		{
			final int z = this.getZ(index);
			index -= (z * this.width * this.length);

			return index / this.width;
		}

		public int getX(int index)
		{
			final int z = this.getZ(index);
			index -= (z * this.width * this.length);

			return index % this.width;
		}

		public int markPos(final int x, final int y, final int z, final int glIndex)
		{
			final int index = this.getIndex(x, y, z);

			this.glIndexes[index] = glIndex;

			return glIndex;
		}
	}
}
