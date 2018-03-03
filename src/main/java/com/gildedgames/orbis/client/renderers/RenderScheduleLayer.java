package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderScheduleLayer implements IWorldRenderer, IScheduleRecordListener
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final IScheduleLayer layer;

	private final IWorldObject parentObject;

	private boolean disabled;

	public RenderScheduleLayer(final IScheduleLayer layer, IScheduleLayerHolder holder, final IWorldObject parentObject)
	{
		this.layer = layer;
		this.parentObject = parentObject;

		if (layer.getScheduleRecord() != null)
		{
			layer.getScheduleRecord().listen(this);
		}

		final RenderFilterRecord renderPositionRecord = new RenderFilterRecord(this.layer.getFilterRecord(), holder, this.parentObject);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.subRenderers.add(renderPositionRecord);
		}
		finally
		{
			w.unlock();
		}

		layer.getScheduleRecord().getSchedules(ScheduleRegion.class).forEach(this::cacheScheduleRegion);
		layer.getScheduleRecord().getSchedules(ScheduleBlueprint.class).forEach(this::cacheScheduleBlueprint);
	}

	private void cacheScheduleBlueprint(ScheduleBlueprint schedule)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderScheduleBlueprint r = new RenderScheduleBlueprint(this.parentObject, schedule);

			this.subRenderers.add(r);
		}
		finally
		{
			w.unlock();
		}
	}

	private void cacheScheduleRegion(ScheduleRegion schedule)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderScheduleRegion r = new RenderScheduleRegion(this.parentObject, schedule);

			this.subRenderers.add(r);
		}
		finally
		{
			w.unlock();
		}
	}

	public void setFocused(boolean focused)
	{
		for (IWorldRenderer r : this.subRenderers)
		{
			if (r instanceof RenderFilterRecord)
			{
				RenderFilterRecord f = (RenderFilterRecord) r;

				f.setFocused(focused);
			}
			else
			{
				r.setDisabled(!focused);
			}
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
		return this.layer;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.parentObject.getShape().getBoundingBox();
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{

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

	@Override
	public void onAddSchedule(ISchedule schedule)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer r = null;

			if (schedule instanceof ScheduleRegion)
			{
				ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

				r = new RenderScheduleRegion(this.parentObject, scheduleRegion);
			}
			else if (schedule instanceof ScheduleBlueprint)
			{
				ScheduleBlueprint scheduleBlueprint = (ScheduleBlueprint) schedule;

				r = new RenderScheduleBlueprint(this.parentObject, scheduleBlueprint);
			}

			this.subRenderers.add(r);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveSchedule(ISchedule schedule)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == schedule)
				{
					toRemove = renderer;
					break;
				}
			}

			this.subRenderers.remove(toRemove);
		}
		finally
		{
			w.unlock();
		}
	}
}
