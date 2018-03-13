package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.util.mc.BlockUtil;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.blueprint.BlueprintRenderCache;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

public class RenderBlueprintBlocks implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final BlockRendererDispatcher blockRenderer;

	private final Blueprint blueprint;

	private final BlueprintRenderCache cache;

	private final float scale = 1.0f;

	public boolean renderBlocks = true;

	public int color = 0x0000FF;

	public Iterable<BlockPos.MutableBlockPos> shapeData;

	private boolean disabled;

	private BlockPos lastMin;

	private int glIndex = -1;

	private Rotation lastRotation;

	public RenderBlueprintBlocks(final Blueprint blueprint, final World world)
	{
		this.blockRenderer = this.mc.getBlockRendererDispatcher();
		this.blueprint = blueprint;
		this.cache = new BlueprintRenderCache(blueprint, world);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			for (Integer id : this.blueprint.getData().getScheduleLayers().keySet())
			{
				IScheduleLayer layer = this.blueprint.getData().getScheduleLayers().get(id);

				if (layer != null)
				{
					RenderScheduleLayer render = new RenderScheduleLayer(layer, this.blueprint, this.blueprint);

					render.setFocused(true);

					this.subRenderers.add(render);
				}
			}
		}
		finally
		{
			w.unlock();
		}

		this.blueprint.getData().entrances().forEach(this::cacheEntrance);
	}

	private void cacheEntrance(Entrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderEntrance render = new RenderEntrance(this.blueprint, entrance);

			this.subRenderers.add(render);
		}
		finally
		{
			w.unlock();
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

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	protected void bindTexture(final ResourceLocation location)
	{
		final TextureManager texturemanager = TileEntityRendererDispatcher.instance.renderEngine;

		if (texturemanager != null)
		{
			texturemanager.bindTexture(location);
		}
	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.blueprint;
	}

	public void cacheRender(final World world, final float partialTicks, boolean useCamera)
	{
		if (!this.renderBlocks)
		{
			return;
		}

		GlStateManager.pushMatrix();

		this.glIndex = GLAllocation.generateDisplayLists(1);
		GlStateManager.glNewList(this.glIndex, GL11.GL_COMPILE);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

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

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		for (final BlockPos pos : this.shapeData)
		{
			this.renderPos(pos, buffer);
		}

		buffer.setTranslation(0, 0, 0);

		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		/** TODO: Temp disabled te rendering because of strange bug when holding a blueprint and loading a world first time**/
		/*final int pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();

		TileEntityRendererDispatcher.instance.preDrawBatch();

		*//** Render tile entities separately since they're done on their own
	 * draw call with the tesselator **//*
		for (final BlockPos pos : this.shapeData)
		{
			this.renderTileEntityIfPossible(pos, partialTicks, pass);
		}

		TileEntityRendererDispatcher.instance.drawBatch(pass);*/

		GlStateManager.glEndList();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();

		this.render(world, partialTicks, useCamera);
	}

	private void renderTileEntityIfPossible(final BlockPos pos, final float partialTicks, final int pass)
	{
		if (!this.renderBlocks)
		{
			return;
		}

		final IBlockState state = this.cache.getBlockState(pos);

		if (state != null && !BlockUtil.isAir(state))
		{
			final Block block = state.getBlock();

			if (block.hasTileEntity(state))
			{
				final TileEntity tileEntity = this.cache.getTileEntity(pos);

				if (!tileEntity.shouldRenderInPass(pass))
				{
					TileEntityRendererDispatcher.instance.render(tileEntity, pos.getX(), pos.getY(), pos.getZ(), partialTicks);
				}
			}
		}
	}

	private void renderPos(final BlockPos renderPos, final BufferBuilder buffer)
	{
		if (!this.renderBlocks)
		{
			return;
		}

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

	@Override
	public IRegion getBoundingBox()
	{
		return this.blueprint.getBoundingBox();
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

	public int getGlIndex()
	{
		return this.glIndex;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.lastRotation != this.blueprint.getRotation())
		{
			this.lastRotation = this.blueprint.getRotation();
		}

		if (this.lastMin == null || this.glIndex == -1)
		{
			this.lastMin = this.blueprint.getMin();
			this.shapeData = BlockPos.getAllInBoxMutable(BlockPos.ORIGIN,
					this.blueprint.getMax().add(-this.blueprint.getMin().getX(), -this.blueprint.getMin().getY(), -this.blueprint.getMin().getZ()));
		}

		if (this.glIndex == -1)
		{
			this.cacheRender(world, partialTicks, useCamera);
			return;
		}

		GodPowerBlueprint bp = PlayerOrbis.get(this.mc.player).powers().getBlueprintPower();

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);

			GlStateManager.translate(this.blueprint.getMin().getX(),
					this.blueprint.getMin().getY(),
					this.blueprint.getMin().getZ());

			RenderUtil.rotateRender(this.blueprint, bp.getPlacingRotation());
		}

		GlStateManager.callList(this.glIndex);

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
		if (!(sub.getRenderedObject() instanceof IScheduleLayer))
		{
			GlStateManager.pushMatrix();

			if (useCamera)
			{
				GlStateManager.translate(this.blueprint.getMin().getX(),
						this.blueprint.getMin().getY(),
						this.blueprint.getMin().getZ());
			}
		}
	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{
		if (!(sub.getRenderedObject() instanceof IScheduleLayer))
		{
			GlStateManager.popMatrix();
		}
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
}
