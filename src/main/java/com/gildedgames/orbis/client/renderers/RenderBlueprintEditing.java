package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolderListener;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderBlueprintEditing implements IWorldRenderer, IScheduleLayerHolderListener
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Blueprint blueprint;

	private boolean disabled = false;

	private IScheduleLayer prevLayer;

	private RenderScheduleLayer prevRender;

	public RenderBlueprintEditing(final Blueprint blueprint)
	{
		this.blueprint = blueprint;

		this.blueprint.listen(this);

		this.onChangeScheduleLayer(null, -1, this.blueprint.getCurrentScheduleLayer(), this.blueprint.getCurrentScheduleLayerIndex());
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
		return this.blueprint;
	}

	@Override
	public void render(final World world, final float partialTicks)
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
		this.blueprint.unlisten(this);
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
	public void onChangeScheduleLayer(IScheduleLayer prevLayer, int prevIndex, IScheduleLayer newLayer, int newIndex)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			final IScheduleLayer layer = this.blueprint.getData().getScheduleLayers().get(newIndex);

			if (layer != null)
			{
				if (this.prevLayer != null && this.prevRender != null)
				{
					this.prevRender.onRemoved();

					this.subRenderers.remove(this.prevRender);
				}

				this.prevLayer = layer;
				this.prevRender = new RenderScheduleLayer(this.prevLayer, this.blueprint, this.blueprint);

				this.subRenderers.add(this.prevRender);
			}
		}
		finally
		{
			w.unlock();
		}
	}
}
