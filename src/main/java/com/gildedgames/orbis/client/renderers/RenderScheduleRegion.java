package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderScheduleRegion implements IWorldRenderer
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final ScheduleRegion scheduleRegion;

	private boolean disabled = false;

	private RenderShape renderShape;

	private Region bb;

	public RenderScheduleRegion(IWorldObject parentObject, final ScheduleRegion scheduleRegion)
	{
		this.scheduleRegion = scheduleRegion;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.bb = new Region(this.scheduleRegion.getBounds());
			this.bb.add(parentObject.getPos());

			this.renderShape = new RenderShape(this.bb);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = scheduleRegion.getColor();
			this.renderShape.colorBorder = scheduleRegion.getColor();

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
		return this.scheduleRegion;
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

		if (playerOrbis.getSelectedSchedule() == this.scheduleRegion && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getSelectPower())
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
