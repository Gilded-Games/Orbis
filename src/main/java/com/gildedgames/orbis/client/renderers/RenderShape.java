package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.shapes.AbstractShape;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.OrbisKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderShape implements IWorldRenderer
{

	private final static BufferBuilder buffer = Tessellator.getInstance().getBuffer();

	private final List<IWorldRenderer> subRenderers = new ArrayList<>();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Minecraft mc = Minecraft.getMinecraft();

	private final int worldObjectID = -1;

	/**
	 * renderGrid: Set to false to unlisten rendering the grid for the region
	 * renderBorder: Set to false to unlisten rendering the border around the region
	 * renderDimensionsAbove: Set to false to unlisten rendering the num x num x num above the region
	 */
	public boolean renderDimensionsAbove = true;

	public float boxAlpha = 0.25F;

	public boolean useCustomColors = false, xyz_box = false, box = true;

	public int colorBorder = -1, colorGrid = -1;

	private int glIndex = -1;

	private Object object;

	private IShape shape;

	private boolean shouldRefresh = false;

	private Iterable<BlockPos.MutableBlockPos> shapeData;

	private BlockPos lastMin;

	private boolean disabled;

	public RenderShape()
	{

	}

	public RenderShape(final IShape shape)
	{
		this.object = shape;
		this.shape = shape;
	}

	public void setShape(final IShape shape)
	{
		if (shape != this.shape)
		{
			this.refresh();
		}

		this.shape = shape;
		this.object = shape;
	}

	public void refresh()
	{
		this.shouldRefresh = true;
	}

	public void renderFully(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.shouldRefresh || this.lastMin == null)
		{
			this.lastMin = this.shape.getBoundingBox().getMin();

			this.shapeData = this.shape.createShapeData();

			this.shouldRefresh = false;
		}

		GlStateManager.pushMatrix();

		final int color = this.useCustomColors ? this.colorGrid : 0xFFFFFF;

		final float red = (color >> 16 & 0xff) / 255F;
		final float green = (color >> 8 & 0xff) / 255F;
		final float blue = (color & 0xff) / 255F;

		this.glIndex = GlStateManager.glGenLists(1);
		GlStateManager.glNewList(this.glIndex, GL11.GL_COMPILE);

		GlStateManager.color(red, green, blue, this.boxAlpha);

		buffer.begin(7, DefaultVertexFormats.POSITION);

		for (final BlockPos pos : this.shapeData)
		{
			this.renderBox(pos, partialTicks);
		}

		Tessellator.getInstance().draw();

		buffer.setTranslation(0, 0, 0);

		GlStateManager.glEndList();

		GlStateManager.popMatrix();

		this.render(world, partialTicks, useCamera);
	}

	private void renderBox(
			final Tessellator tessellator, final double x1, final double y1, final double z1, final double x2, final double y2,
			final double z2, final int color1, final int color2, final int color3)
	{
		buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

		if (this.box)
		{
			buffer.pos(x1, y1, z1).color(color2, color2, color2, 0.0F).endVertex();
			buffer.pos(x1, y1, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y1, z1).color(color2, color3, color3, color1).endVertex();
			buffer.pos(x2, y1, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y1, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y1, z1).color(color3, color3, color2, color1).endVertex();
			buffer.pos(x1, y2, z1).color(color3, color2, color3, color1).endVertex();
			buffer.pos(x2, y2, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y2, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y2, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y2, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y2, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x1, y1, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y1, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y2, z2).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y2, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y1, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y1, z1).color(color2, color2, color2, 0.0F).endVertex();
		}

		if (this.xyz_box)
		{
			buffer.pos(x1, y1, z1).color(color2, color2, color2, color1).endVertex();
			buffer.pos(x2, y1, z1).color(color2, color3, color3, color1).endVertex();
			buffer.pos(x2, y1, z2).color(color2, color2, color2, 0.0F).endVertex();
			buffer.pos(x1, y1, z2).color(color2, color2, color2, 0.0F).endVertex();
			buffer.pos(x1, y1, z1).color(color3, color3, color2, color1).endVertex();
			buffer.pos(x1, y2, z1).color(color3, color2, color3, color1).endVertex();
		}

		tessellator.draw();
	}

	public void renderBox(final BlockPos pos, final float partialTicks)
	{
		final int minX = pos.getX();
		final int minY = pos.getY();
		final int minZ = pos.getZ();

		final int maxX = pos.getX() + 1;
		final int maxY = pos.getY() + 1;
		final int maxZ = pos.getZ() + 1;

		final float stretch = 0.01F;

		final AxisAlignedBB bounds = new AxisAlignedBB(minX - stretch, minY - stretch, minZ - stretch, maxX + stretch, maxY + stretch, maxZ + stretch);

		if (!this.shape.contains(minX - 1, minY, minZ))
		{
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();

			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();

			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();
		}

		if (!this.shape.contains(minX + 1, minY, minZ))
		{
			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();
		}

		if (!this.shape.contains(minX, minY, minZ - 1))
		{
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();
		}

		if (!this.shape.contains(minX, minY, minZ + 1))
		{
			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();

			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();

			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();
		}

		if (!this.shape.contains(minX, minY - 1, minZ))
		{
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();

			buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).endVertex();
		}

		if (!this.shape.contains(minX, minY + 1, minZ))
		{
			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();

			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).endVertex();

			buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).endVertex();
			buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).endVertex();
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
	public Object getRenderedObject()
	{
		return this.object;
	}

	@Override
	public IRegion getBoundingBox()
	{
		if (this.shape == null)
		{
			return Region.ORIGIN;
		}

		return this.shape.getBoundingBox();
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
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.shape == null)
		{
			return;
		}

		if (this.shouldRefresh || this.lastMin == null)
		{
			if (this.lastMin != null)
			{
				GlStateManager.glDeleteLists(this.glIndex, 1);
				this.glIndex = -1;
			}

			this.lastMin = this.shape.getBoundingBox().getMin();

			this.shapeData = this.shape.createShapeData();

			this.shouldRefresh = false;
		}

		if (this.glIndex == -1)
		{
			this.renderFully(world, partialTicks, useCamera);
			return;
		}

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			if (!this.lastMin.equals(this.shape.getBoundingBox().getMin()))
			{
				GlStateManager.translate(this.shape.getBoundingBox().getMin().getX() - this.lastMin.getX(),
						this.shape.getBoundingBox().getMin().getY() - this.lastMin.getY(),
						this.shape.getBoundingBox().getMin().getZ() - this.lastMin.getZ());
			}

			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
		}

		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

		GlStateManager.disableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.disableDepth();
		}

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();

		GlStateManager.callList(this.glIndex);

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.enableDepth();
		}

		GlStateManager.enableLighting();
		GlStateManager.enableAlpha();

		GlStateManager.translate(0, 0, 0);

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
		}

		BlockPos min = this.shape.getBoundingBox().getMin();
		BlockPos max = this.shape.getBoundingBox().getMax();

		if (this.shape instanceof AbstractShape)
		{
			final AbstractShape abShape = (AbstractShape) this.shape;

			min = abShape.getRenderBoxMin();
			max = abShape.getRenderBoxMax();
		}

		final BlockPos fromBB = min.add(-1, -1, -1);
		final BlockPos toBB = max.add(1, 1, 1);

		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager
				.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.disableDepth();
		}

		GlStateManager.depthMask(true);

		GlStateManager.glLineWidth(2.0F);

		this.renderBox(Tessellator.getInstance(), fromBB.getX() + 0.98, fromBB.getY() + 0.98, fromBB.getZ() + 0.98,
				toBB.getX() + 0.02, toBB.getY() + 0.02, toBB.getZ() + 0.02,
				255, 223, 127);

		GlStateManager.glLineWidth(1.0F);

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.enableDepth();
		}

		GlStateManager.disableLighting();

		GlStateManager.popMatrix();

		/*if (this.renderDimensionsAbove)
		{
			RenderUtil.renderDimensionsAbove(this.shape.getBoundingBox(), partialTicks);

			if (this.worldObjectID == -1 && this.getRenderedObject() instanceof IWorldObject)
			{
				final IWorldObject obj = (IWorldObject) this.getRenderedObject();

				final WorldObjectManager manager = WorldObjectManager.get(this.world);
				final IWorldObjectGroup group = manager;

				if (group.hasObject(obj))
				{
					this.worldObjectID = group.getID(obj);
				}
			}

			RenderUtil.renderTextAbove(this.shape.getBoundingBox(), "World ID: " + String.valueOf(this.worldObjectID), 2.0D, partialTicks);
		}*/
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
	public void onRemoved()
	{
		if (this.glIndex != -1)
		{
			GlStateManager.glDeleteLists(this.glIndex, 1);
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
