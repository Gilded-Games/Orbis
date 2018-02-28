package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolder;
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

public class RenderScheduleLayer implements IWorldRenderer
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

		final RenderFilterRecord renderPositionRecord = new RenderFilterRecord(this.layer.getDataRecord(), holder, this.parentObject);

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
	}

	public void setFocused(boolean focused)
	{
		for (IWorldRenderer r : this.subRenderers)
		{
			if (r instanceof RenderFilterRecord)
			{
				RenderFilterRecord f = (RenderFilterRecord) r;

				((RenderFilterRecord) r).setFocused(focused);
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
}
