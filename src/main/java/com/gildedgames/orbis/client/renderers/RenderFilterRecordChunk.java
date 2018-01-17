package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IPositionRecord;
import com.gildedgames.orbis.api.util.mc.BlockUtil;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.util.BlockRenderUtil;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderFilterRecordChunk implements IWorldRenderer
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final BlockRenderUtil blockRenderer = new BlockRenderUtil(
			ObfuscationReflectionHelper.getPrivateValue(BlockModelRenderer.class, mc.getBlockRendererDispatcher().getBlockModelRenderer(), 0));

	private final IPositionRecord<BlockFilter> positionRecord;

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Random rand = new Random();

	private final IWorldObject parentObject;

	private final FilterRecordAccess stateAccess;

	private boolean disabled;

	private BlockPos lastPos;

	private BlockPos chunkPos;

	private int glIndex = -1;

	public RenderFilterRecordChunk(final IPositionRecord<BlockFilter> positionRecord, final IWorldObject parentObject, BlockPos chunkPos)
	{
		this.positionRecord = positionRecord;
		this.parentObject = parentObject;

		this.chunkPos = chunkPos;
		this.stateAccess = new FilterRecordAccess(mc.world, positionRecord, parentObject.getPos());
	}

	protected void bindTexture(final ResourceLocation location)
	{
		final TextureManager texturemanager = TileEntityRendererDispatcher.instance.renderEngine;

		if (texturemanager != null)
		{
			texturemanager.bindTexture(location);
		}
	}

	private void cacheRenderedBlocks()
	{
		GlStateManager.pushMatrix();

		this.glIndex = GLAllocation.generateDisplayLists(1);
		GlStateManager.glNewList(this.glIndex, GL11.GL_COMPILE);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		}
		else
		{
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		int chunkX = this.chunkPos.getX() * 16;
		int chunkY = this.chunkPos.getY() * 16;
		int chunkZ = this.chunkPos.getZ() * 16;

		int minX = this.parentObject.getPos().getX();
		int minY = this.parentObject.getPos().getY();
		int minZ = this.parentObject.getPos().getZ();

		int maxX = minX + this.positionRecord.getBoundingBox().getMax().getX();
		int maxY = minY + this.positionRecord.getBoundingBox().getMax().getY();
		int maxZ = minZ + this.positionRecord.getBoundingBox().getMax().getZ();

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				for (int y = 0; y < 16; y++)
				{
					int xDif = x + chunkX;
					int yDif = y + chunkY;
					int zDif = z + chunkZ;

					if (xDif <= maxX - minX && yDif <= maxY - minY && zDif <= maxZ - minZ)
					{
						BlockFilter filter = this.positionRecord.get(xDif, yDif, zDif);

						if (filter != null)
						{
							pos.setPos(xDif + minX, yDif + minY, zDif + minZ);

							this.renderPos(filter, pos, buffer);
						}
					}
				}
			}
		}

		buffer.setTranslation(0, 0, 0);

		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.glEndList();

		GlStateManager.popMatrix();
	}

	private void renderPos(final BlockFilter filter, final BlockPos renderPos, BufferBuilder buffer)
	{
		this.rand.setSeed((long) renderPos.getX() * 341873128712L + (long) renderPos.getY() * 23289687541L + (long) renderPos.getZ() * 132897987541L);

		final IBlockState state = filter.getSample(mc.world, this.rand, Blocks.AIR.getDefaultState());

		if (state != null && !BlockUtil.isAir(state) && !BlockUtil.isVoid(state) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
		{
			GlStateManager.enableLighting();

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			RenderHelper.disableStandardItemLighting();

			GlStateManager.enableCull();

			final IBakedModel modelBaked = mc.getBlockRendererDispatcher().getModelForState(state);

			blockRenderer.renderModel(this.stateAccess, modelBaked, state, renderPos, buffer, true, MathHelper.getPositionRandom(renderPos));
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
		if (this.lastPos == null)
		{
			this.lastPos = this.parentObject.getPos();
		}

		if (this.glIndex == -1)
		{
			this.cacheRenderedBlocks();
		}

		final double offsetPlayerX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;

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

		//GlStateManager.enableLighting();//

		//RenderHelper.disableStandardItemLighting();

		GlStateManager.callList(this.glIndex);

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
		if (this.glIndex != -1)
		{
			GLAllocation.deleteDisplayLists(this.glIndex, 1);
			this.glIndex = -1;
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
}
