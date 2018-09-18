package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.OrbisKeyBindings;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.data.pathway.IEntrance;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderEntrance implements IWorldRenderer
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Minecraft mc = Minecraft.getMinecraft();

	private final IEntrance entrance;

	private boolean disabled = false;

	private RenderShape renderShape;

	private IWorldObject parent;

	public RenderEntrance(IWorldObject parentObject, final IEntrance entrance)
	{
		this.entrance = entrance;
		this.parent = parentObject;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.renderShape = new RenderShape(new Region(this.entrance.getBounds()))
			{
				@Override
				public IRegion getBoundingBox()
				{
					return RenderEntrance.this.parent.getShape().getBoundingBox();
				}
			};

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.entrance.getColor();
			this.renderShape.colorBorder = this.entrance.getColor();

			this.subRenderers.add(this.renderShape);
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

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.entrance;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.parent.getShape().getBoundingBox();
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
		}

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.disableDepth();
		}

		GlStateManager.depthMask(true);

		GlStateManager.glLineWidth(5.0F);

		double xDir = 0;
		double yDir = 0;
		double zDir = 0;

		for (EnumFacing face : this.entrance.getFacing().getFacings())
		{
			xDir += 0.25D * face.getDirectionVec().getX();
			yDir += 0.25D * face.getDirectionVec().getY();
			zDir += 0.25D * face.getDirectionVec().getZ();
		}

		double offsetX = this.entrance.getBounds().getMin().getX() + (double) this.entrance.getBounds().getWidth() / 2.0D;
		double offsetY = this.entrance.getBounds().getMin().getY() + (double) this.entrance.getBounds().getHeight() / 2.0D;
		double offsetZ = this.entrance.getBounds().getMin().getZ() + (double) this.entrance.getBounds().getLength() / 2.0D;

		double tailX = this.parent.getShape().getBoundingBox().getMin().getX() - xDir + offsetX;
		double tailY = this.parent.getShape().getBoundingBox().getMin().getY() - yDir + offsetY;
		double tailZ = this.parent.getShape().getBoundingBox().getMin().getZ() - zDir + offsetZ;

		double centerX = this.parent.getShape().getBoundingBox().getMin().getX() + offsetX;
		double centerY = this.parent.getShape().getBoundingBox().getMin().getY() + offsetY;
		double centerZ = this.parent.getShape().getBoundingBox().getMin().getZ() + offsetZ;

		double tipX = this.parent.getShape().getBoundingBox().getMin().getX() + xDir + offsetX;
		double tipY = this.parent.getShape().getBoundingBox().getMin().getY() + yDir + offsetY;
		double tipZ = this.parent.getShape().getBoundingBox().getMin().getZ() + zDir + offsetZ;

		this.drawLine(tailX, tailY, tailZ, centerX, centerY, centerZ, 255, 255, 255);
		this.drawLine(centerX, centerY, centerZ, tipX, tipY, tipZ, 0, 255, 255);

		/*double dx = tipX - tailX;
		double dy = tipY - tailY;
		double dz = tipZ - tailZ;

		double distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));

		double arrowLength = 0.3D;

		Vec3d location = new Vec3d(tipX, tipY, tipZ);
		Vec3d position = new Vec3d(tailX, tailY, tailZ);

		Vec3d a = position.subtract(location).normalize();
		Vec3d b = new Vec3d(dx / distance, dy / distance, dz / distance);

		double theta = 0;

		double rad = Math.toRadians(35);

		double x = tipX - arrowLength * Math.sin(theta + rad) * Math.cos(theta + rad);
		double y = tipY - arrowLength * Math.sin(theta + rad) * Math.sin(theta + rad);
		double z = tipZ - arrowLength * Math.cos(theta + rad);

		double phi2 = Math.toRadians(-35);

		double x2 = tipX - arrowLength * Math.sin(theta + phi2) * Math.cos(theta + phi2);
		double y2 = tipY - arrowLength * Math.sin(theta + phi2) * Math.sin(theta + phi2);
		double z2 = tipZ - arrowLength * Math.sin(theta + phi2);

		this.drawLine(tipX, tipY, tipZ, x, y, z);
		this.drawLine(tipX, tipY, tipZ, x2, y2, z2);*/

		GlStateManager.glLineWidth(1.0F);

		if (OrbisKeyBindings.keyBindControl.isKeyDown())
		{
			GlStateManager.enableDepth();
		}

		GlStateManager.popMatrix();

		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedEntrance() == this.entrance && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getEntrancePower())
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
	}

	@Override
	public void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(
				this.parent.getPos().getX(),
				this.parent.getPos().getY(),
				this.parent.getPos().getZ()
		);
	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{
		GlStateManager.popMatrix();
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

	private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int r, int g, int b)
	{
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(x1, y1, z1).color(r, g, b, 255).endVertex();
		buffer.pos(x2, y2, z2).color(r, g, b, 255).endVertex();

		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
	}
}
