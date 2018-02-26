package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderEntityItem implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final EntityEntry entry;

	public float scale = 1.0F;

	private boolean disabled;

	private int glIndex = -1;

	private boolean shouldRefresh = false;

	private Entity entity;

	private IRegion bb;

	public RenderEntityItem(EntityEntry entry)
	{
		this.entry = entry;
		this.entity = this.entry.newInstance(this.mc.world);

		AxisAlignedBB bb = this.entity.getEntityBoundingBox();

		double width = bb.maxX - bb.minX;
		double height = bb.maxY - bb.minY;
		double length = bb.maxZ - bb.minZ;

		this.bb = new Region(BlockPos.ORIGIN, new BlockPos(width, height, length));
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

	public void refresh()
	{
		this.shouldRefresh = true;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.entry;
	}

	public void cacheRender(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.shouldRefresh)
		{
			this.shouldRefresh = false;
		}

		//this.entity.onUpdate();

		GlStateManager.enableColorMaterial();

		GlStateManager.pushMatrix();

		GlStateManager.enableColorMaterial();

		this.glIndex = GlStateManager.glGenLists(1);
		GlStateManager.glNewList(this.glIndex, GL11.GL_COMPILE);

		RenderHelper.enableStandardItemLighting();

		GlStateManager.translate(0.0F, 0.0F, 0.0F);

		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();

		//rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);

		rendermanager.renderEntity(this.entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);

		rendermanager.setRenderShadow(true);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GlStateManager.glEndList();

		GlStateManager.popMatrix();

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		//this.renderSubRenderers(world, partialTicks, useCamera);
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.bb;
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
		//if (this.glIndex == -1 || this.shouldRefresh)
		{
			this.cacheRender(world, partialTicks, useCamera);
			//return;
		}

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
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
	public void preRenderSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	public void transformForWorld()
	{
		int maxval = Math.max(this.bb.getWidth(), this.bb.getHeight());
		maxval = Math.max(this.bb.getLength(), maxval);

		final float scalefactor = Math.min(1, (this.scale / maxval));

		GlStateManager.translate(0.5F, 0.65F, 0.5F);

		GlStateManager.scale(0.6F, 0.6F, 0.6F);
		GlStateManager.scale(scalefactor, scalefactor, scalefactor);

		GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);

		GlStateManager.translate(-this.bb.getWidth() / 2.f, -this.bb.getHeight() / 2.f, -this.bb.getLength() / 2.f);
	}

	public void transformForGui()
	{
		int maxval = Math.max(this.bb.getWidth(), this.bb.getHeight());
		maxval = Math.max(this.bb.getLength(), maxval);

		final float scalefactor = Math.min(1, (this.scale / maxval));

		GlStateManager.translate(0.5F, 0.5F, 0.5F);

		GlStateManager.scale(0.7, 0.7F, 0.7F);
		GlStateManager.scale(scalefactor, scalefactor, scalefactor);

		GlStateManager.rotate(45.0F, 0.0F, -1.0F, 0.0F);
		GlStateManager.rotate(30.0F, 1.0F, 0.0F, -1.0F);

		GlStateManager.translate(-this.bb.getWidth() / 2.f, -this.bb.getHeight() / 2.f, -this.bb.getLength() / 2.f);
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
