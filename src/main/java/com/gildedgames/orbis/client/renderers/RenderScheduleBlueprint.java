package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.OrbisClientCaches;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.WorldRenderHelp;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.data.schedules.ScheduleBlueprint;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderScheduleBlueprint implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final ScheduleBlueprint scheduleBlueprint;

	private boolean disabled = false;

	private RenderShape renderShape;

	private Region bb;

	private IWorldObject parentObject;

	public RenderScheduleBlueprint(IWorldObject parentObject, final ScheduleBlueprint scheduleBlueprint)
	{
		this.parentObject = parentObject;
		this.scheduleBlueprint = scheduleBlueprint;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.bb = new Region(this.scheduleBlueprint.getBounds());
			this.bb.add(parentObject.getPos());

			this.renderShape = new RenderShape(this.bb);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = scheduleBlueprint.getColor();
			this.renderShape.colorBorder = scheduleBlueprint.getColor();

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
		return this.scheduleBlueprint;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.bb;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedSchedule() == this.scheduleBlueprint && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getSelectPower())
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

		try
		{
			final BlueprintDataPalette palette = this.scheduleBlueprint.getPalette();

			if (palette == null || palette.getIDs().size() <= 0)
			{
				return;
			}

			final int target = (int) ((System.currentTimeMillis() / 1000) % palette.getIDs().size());
			int i = 0;

			IDataIdentifier id = null;

			for (final IDataIdentifier itId : palette.getIDs())
			{
				if (i == target)
				{
					id = itId;
				}

				i++;
			}

			final Optional<RenderBlueprintBlocks> opt = OrbisClientCaches.getBlueprintRenders().get(id);

			if (!opt.isPresent())
			{
				return;
			}

			final RenderBlueprintBlocks blueprint = opt.get();

			final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
			final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
			final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

			GlStateManager.pushMatrix();

			if (useCamera)
			{
				GlStateManager
						.translate(this.bb.getMin().getX(), this.bb.getMin().getY(),
								this.bb.getMin().getZ());
				GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
			}

			blueprint.render(this.mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

			WorldRenderHelp.renderSubRenderers(blueprint);

			if (useCamera)
			{
				GlStateManager.translate(0, 0, 0);
			}

			GlStateManager.resetColor();

			GlStateManager.popMatrix();
		}
		catch (final ExecutionException e)
		{
			OrbisCore.LOGGER.error(e);
		}
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
