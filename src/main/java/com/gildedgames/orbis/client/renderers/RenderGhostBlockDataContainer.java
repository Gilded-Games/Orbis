package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.renderers.blueprint.BlockDataContainerAccess;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.util.mc.BlockUtil;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderGhostBlockDataContainer
		implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final GhostBlockDataContainer data;

	private final BlockRendererDispatcher blockRenderer;

	public Iterable<BlockPos.MutableBlockPos> shapeData;

	private BlockDataContainerAccess cache;

	private boolean disabled = false;

	private RenderShape renderShape;

	private CachedRender cachedBlocks;

	private boolean hasResetBlockCache;

	private BlockPos lastMin;

	private Rotation lastRotation;

	public RenderGhostBlockDataContainer(final GhostBlockDataContainer data)
	{
		this.blockRenderer = this.mc.getBlockRendererDispatcher();
		this.data = data;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.renderShape = new RenderShape(this.data);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.data.getColor();
			this.renderShape.colorBorder = this.data.getColor();

			this.subRenderers.add(this.renderShape);
		}
		finally
		{
			w.unlock();
		}
	}

	private void renderPos(final BlockPos renderPos, final BufferBuilder buffer)
	{
		final IBlockState state = this.cache.getBlockState(renderPos);

		if (state != null && !BlockUtil.isAir(state) && !BlockUtil.isVoid(state) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
		{
			//Thank you Ivorius for the rendering of blocks code <3333
			final IBakedModel modelBaked = this.blockRenderer.getModelForState(state);

			final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer()
					.renderModel(this.cache, modelBaked, state, renderPos, buffer, true, MathHelper.getPositionRandom(renderPos));
		}
	}

	protected void bindTexture(final ResourceLocation location)
	{
		final TextureManager texturemanager = TileEntityRendererDispatcher.instance.renderEngine;

		if (texturemanager != null)
		{
			texturemanager.bindTexture(location);
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
		return this.data;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.data;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.cache == null)
		{
			this.cache = new BlockDataContainerAccess(this.data.getBlockDataContainer(), world);
		}
		else if (this.cache.getWorld() != world)
		{
			this.cache = new BlockDataContainerAccess(this.data.getBlockDataContainer(), world);
		}

		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedRegion() == this.data && (playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getBlueprintPower()
				|| playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getSelectPower()))
		{
			boolean refresh = this.renderShape.boxAlpha == 0.25F;

			this.renderShape.boxAlpha = 0.5F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}
		else
		{
			boolean refresh = this.renderShape.boxAlpha == 0.5F;

			this.renderShape.boxAlpha = 0.25F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}

		if (this.lastRotation != this.data.getRotation())
		{
			this.lastRotation = this.data.getRotation();
		}

		if (this.lastMin == null || this.cachedBlocks == null || this.hasResetBlockCache)
		{
			this.lastMin = this.data.getMin();
			this.shapeData = BlockPos.getAllInBoxMutable(BlockPos.ORIGIN,
					this.data.getMax().add(-this.data.getMin().getX() + 10, -this.data.getMin().getY() + 10, -this.data.getMin().getZ() + 10));

			this.hasResetBlockCache = false;
		}

		if (this.cachedBlocks == null)
		{
			this.cachedBlocks = new CachedRender(DefaultVertexFormats.BLOCK,
					(VertexFormat format, BufferBuilder buffer, World theWorld, float thePartialTicks) ->
					{
						for (final BlockPos pos : this.shapeData)
						{
							this.renderPos(pos, buffer);
						}
					});
		}

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GodPowerBlueprint bp = PlayerOrbis.get(this.mc.player).powers().getBlueprintPower();

			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);

			GlStateManager.translate(this.data.getMin().getX(),
					this.data.getMin().getY(),
					this.data.getMin().getZ());

			RenderUtil.rotateRender(this.data, bp.getPlacingRotation());
		}

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();

		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		}
		else
		{
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		this.cachedBlocks.render(world, partialTicks);

		RenderHelper.enableStandardItemLighting();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (useCamera)
		{
			GlStateManager.translate(0, 0, 0);
		}

		GlStateManager.resetColor();

		GlStateManager.popMatrix();
	}

	@Override
	public void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void preRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

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
