package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.OrbisKeyBindings;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.schedules.IBlueprint;
import com.gildedgames.orbis_api.data.schedules.IPositionRecord;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.util.mc.BlockUtil;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderStateRecordChunk implements IWorldRenderer
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();

	private final IPositionRecord<IBlockState> stateRecord;

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final IWorldObject parentObject;

	private final StateRecordAccess stateAccess;

	private boolean disabled;

	private BlockPos lastPos;

	private BlockPos chunkPos;

	private int glIndex = -1;

	private boolean shouldRedraw, focused, rotateData;

	private IScheduleLayer layer;

	private IBlueprint blueprint;

	public RenderStateRecordChunk(IBlueprint blueprint, IScheduleLayer layer, final IPositionRecord<IBlockState> stateRecord, final IWorldObject parentObject,
			BlockPos chunkPos,
			boolean rotateData)
	{
		this.blueprint = blueprint;
		this.layer = layer;
		this.stateRecord = stateRecord;
		this.parentObject = parentObject;

		this.chunkPos = chunkPos;
		this.stateAccess = new StateRecordAccess(mc.world, stateRecord, BlockPos.ORIGIN);
		this.rotateData = rotateData;
	}

	public boolean isFocused()
	{
		return this.focused;
	}

	public void setFocused(boolean focused)
	{
		boolean willRedraw = this.focused != focused;

		this.focused = focused;

		if (willRedraw)
		{
			this.redraw();
		}
	}

	public void redraw()
	{
		this.shouldRedraw = true;
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

		int minX = 0;//this.parentObject.getPos().getX();
		int minY = 0;//this.parentObject.getPos().getY();
		int minZ = 0;//this.parentObject.getPos().getZ();

		int maxX = minX + this.stateRecord.getBoundingBox().getMax().getX();
		int maxY = minY + this.stateRecord.getBoundingBox().getMax().getY();
		int maxZ = minZ + this.stateRecord.getBoundingBox().getMax().getZ();

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				for (int y = 0; y < 16; y++)
				{
					int xDif = x + chunkX;
					int yDif = y + chunkY;
					int zDif = z + chunkZ;

					if (xDif <= maxX - minX && yDif <= maxY - minY && zDif <= maxZ - minZ && xDif >= 0 && yDif >= 0 && zDif >= 0)
					{
						IBlockState state = this.stateRecord.get(xDif, yDif, zDif);

						if (state != null)
						{
							pos.setPos(xDif + minX, yDif + minY, zDif + minZ);

							this.renderPos(state, pos, buffer);
						}
					}
				}
			}
		}

		ForgeHooksClient.setRenderLayer(origLayer);

		buffer.setTranslation(0, 0, 0);

		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.glEndList();

		GlStateManager.popMatrix();
	}

	private void renderPos(final IBlockState state, final BlockPos renderPos, BufferBuilder buffer)
	{
		if (state != null && !BlockUtil.isAir(state) && !BlockUtil.isVoid(state) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
		{
			GlStateManager.enableLighting();

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			RenderHelper.disableStandardItemLighting();

			GlStateManager.enableCull();

			final IBakedModel modelBaked = mc.getBlockRendererDispatcher().getModelForState(state);

			blockRenderer.getBlockModelRenderer()
					.renderModel(this.stateAccess, modelBaked, state, renderPos, buffer, true, MathHelper.getPositionRandom(renderPos));
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
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		if (!this.focused && OrbisKeyBindings.keyBindControl.isKeyDown() || (!this.layer.isVisible() && !this.focused))
		{
			return;
		}

		GlStateManager.resetColor();

		if (this.lastPos == null)
		{
			this.lastPos = this.parentObject.getPos();
		}

		if (this.shouldRedraw)
		{
			this.onRemoved();
			this.shouldRedraw = false;
		}

		if (this.glIndex == -1)
		{
			this.cacheRenderedBlocks();
		}

		final double offsetPlayerX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager.translate(-0.005F, -0.005F, -0.005F);
			GlStateManager.scale(1.01F, 1.01F, 1.01F);
			GodPowerBlueprint bp = PlayerOrbis.get(mc.player).powers().getBlueprintPower();

			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);

			GlStateManager.translate(this.parentObject.getPos().getX(),
					this.parentObject.getPos().getY(),
					this.parentObject.getPos().getZ());

			if (this.rotateData)
			{
				RenderUtil.rotateRender(this.parentObject.getShape().getBoundingBox(), bp.getPlacingRotation());
			}
		}

		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();

		GlStateManager.enableNormalize();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		if (!this.focused && this.blueprint.getData().getBlueprintMetadata().getLayerTransparencyVar().getData())
		{
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

			GL14.glBlendColor(1F, 1F, 1F, 0.5F);
		}

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.callList(this.glIndex);

		if (!this.focused && this.blueprint.getData().getBlueprintMetadata().getLayerTransparencyVar().getData())
		{
			GL14.glBlendColor(1F, 1F, 1F, 1F);

			GlStateManager.disableBlend();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (useCamera)
		{
			GlStateManager.translate(0, 0, 0);
		}

		GlStateManager.disableBlend();
		RenderHelper.enableStandardItemLighting();

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
