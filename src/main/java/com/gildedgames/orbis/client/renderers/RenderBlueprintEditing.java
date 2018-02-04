package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.IBlueprintDataListener;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolderListener;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.IColored;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderBlueprintEditing implements IWorldRenderer, IScheduleLayerHolderListener, IBlueprintDataListener
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Blueprint blueprint;

	private boolean disabled = false;

	private IScheduleLayer focusedLayer;

	private RenderScheduleLayer focusedRender;

	public RenderBlueprintEditing(final Blueprint blueprint)
	{
		this.blueprint = blueprint;

		this.blueprint.listen(this);
		this.blueprint.getData().listen(this);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			for (Integer id : this.blueprint.getData().getScheduleLayers().keySet())
			{
				IScheduleLayer layer = this.blueprint.getData().getScheduleLayers().get(id);

				if (layer != null)
				{
					this.subRenderers.add(new RenderScheduleLayer(layer, this.blueprint, this.blueprint));
				}
			}

			this.blueprint.getData().getEntrances().forEach(this::onAddEntrance);
		}
		finally
		{
			w.unlock();
		}

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
				RenderScheduleLayer found = null;

				for (IWorldRenderer r : this.subRenderers)
				{
					if (r instanceof RenderScheduleLayer)
					{
						RenderScheduleLayer s = (RenderScheduleLayer) r;

						if (s.getRenderedObject() == layer)
						{
							found = s;
							break;
						}
					}
				}

				if (this.focusedLayer != null && this.focusedRender != null)
				{
					this.focusedRender.setFocused(false);
				}

				this.focusedLayer = layer;

				if (found == null)
				{
					this.focusedRender = new RenderScheduleLayer(this.focusedLayer, this.blueprint, this.blueprint);
					this.focusedRender.setFocused(true);

					this.subRenderers.add(this.focusedRender);
				}
				else
				{
					this.focusedRender = found;
					this.focusedRender.setFocused(true);
				}
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveScheduleLayer(IScheduleLayer layer, int index)
	{

	}

	@Override
	public void onAddScheduleLayer(IScheduleLayer layer, int index)
	{

	}

	@Override
	public void onDataChanged()
	{

	}

	@Override
	public void onAddEntrance(IRegion entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderShape shape = new RenderShape(entrance);

			shape.useCustomColors = true;

			if (entrance instanceof IColored)
			{
				shape.colorBorder = ((IColored) entrance).getColor();
				shape.colorGrid = ((IColored) entrance).getColor();
			}

			this.subRenderers.add(shape);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveEntrance(IRegion entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == entrance)
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
