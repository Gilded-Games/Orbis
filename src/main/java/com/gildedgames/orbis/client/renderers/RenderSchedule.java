package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.OrbisClientCaches;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.common.util.WorldRenderHelp;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.data.region.Region;
import com.gildedgames.orbis.lib.data.schedules.ISchedule;
import com.gildedgames.orbis.lib.data.schedules.ScheduleBlueprint;
import com.gildedgames.orbis.lib.data.schedules.ScheduleEntranceHolder;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderSchedule<T extends ISchedule> implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final T schedule;

	private boolean disabled = false;

	private RenderShape renderShape;

	private Region bb;

	private IWorldObject parentObject;
	private Collection<IDataIdentifier> dataIdsToRender;
	private Rotation rotation;

	public static RenderSchedule<ScheduleBlueprint> create(IWorldObject parentObject, final ScheduleBlueprint schedule) {
		return new RenderSchedule<>(parentObject, schedule, schedule.getPalette().getIDs(), schedule.getColor(), schedule.getRotation());
	}

	public static RenderSchedule<ScheduleEntranceHolder> create(IWorldObject parentObject, final ScheduleEntranceHolder schedule) {
		Set<IDataIdentifier> id = Collections.singleton(schedule.getEntranceHolder());
		return new RenderSchedule<>(parentObject, schedule, id, schedule.getColor(), schedule.getRotation());
	}

	public RenderSchedule(IWorldObject parentObject, final T schedule, Collection<IDataIdentifier> dataIdsToRender, int color, Rotation rotation)
	{
		this.parentObject = parentObject;
		this.schedule = schedule;
		this.dataIdsToRender = dataIdsToRender;
		this.rotation = rotation;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.bb = new Region(this.schedule.getBounds());
			this.bb.add(parentObject.getPos());

			this.renderShape = new RenderShape(this.bb);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = color;
			this.renderShape.colorBorder = color;

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
		return this.schedule;
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
		IGodPower power = playerOrbis.powers().getCurrentPower();

		if (playerOrbis.getSelectedSchedule() == this.schedule && (power == playerOrbis.powers().getSelectPower() || power == playerOrbis.powers().getEntrancePower()))
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
			final Collection<IDataIdentifier> ids = this.dataIdsToRender;

			if (ids == null || ids.size() <= 0)
			{
				return;
			}

			final int target = (int) ((System.currentTimeMillis() / 1000) % ids.size());
			int i = 0;

			IDataIdentifier id = null;

			for (final IDataIdentifier itId : ids)
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

			this.renderBlueprint(useCamera, partialTicks, blueprint);
		}
		catch (final ExecutionException e)
		{
			OrbisCore.LOGGER.error(e);
		}


	}

	private void renderBlueprint(boolean useCamera, float partialTicks, RenderBlueprintBlocks blueprint) {
		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		GlStateManager.pushMatrix();

		if (useCamera)
		{
			GlStateManager
					.translate(this.bb.getMin().getX(), this.bb.getMin().getY(),
							this.bb.getMin().getZ());

			float angle = 0.0F;

			switch (this.rotation)
			{
				case NONE:
					angle = 0.0F;
					break;
				case CLOCKWISE_90:
					angle = 270.0F;
					break;
				case CLOCKWISE_180:
					angle = 180.0F;
					break;
				case COUNTERCLOCKWISE_90:
					angle = 90.0F;
					break;
			}

			GlStateManager.translate(this.bb.getWidth() / 2.0F, this.bb.getHeight() / 2.0F, this.bb.getLength() / 2.0F);
			GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(-this.bb.getWidth() / 2.0F, -this.bb.getHeight() / 2.0F, -this.bb.getLength() / 2.0F);

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
