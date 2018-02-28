package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.blueprint.IBlueprintDataListener;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IColored;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.ISchedule;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolderListener;
import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.api.data.shapes.CuboidShape;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
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

	private RenderShape renderShape;

	private RenderShape renderOutskirts;

	public RenderBlueprintEditing(final Blueprint blueprint)
	{
		this.blueprint = blueprint;

		this.blueprint.listen(this);
		this.blueprint.getData().listen(this);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.renderShape = new RenderShape(this.blueprint);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.blueprint.getColor();
			this.renderShape.colorBorder = this.blueprint.getColor();

			this.subRenderers.add(this.renderShape);

			this.renderOutskirts = new RenderShape(new CuboidShape(this.blueprint.getMin().add(1, 1, 1), this.blueprint.getMax().add(-1, -1, -1), false));

			this.renderOutskirts.useCustomColors = true;

			this.renderOutskirts.colorGrid = 0x000000;
			this.renderOutskirts.colorBorder = 0x000000;

			this.renderOutskirts.box = false;

			if (this.blueprint.getLength() >= 3 && this.blueprint.getWidth() >= 3)
			{
				this.subRenderers.add(this.renderOutskirts);
			}

			for (Integer id : this.blueprint.getData().getScheduleLayers().keySet())
			{
				IScheduleLayer layer = this.blueprint.getData().getScheduleLayers().get(id);

				if (layer != null)
				{
					this.subRenderers.add(new RenderScheduleLayer(layer, this.blueprint, this.blueprint));
				}
			}

			this.blueprint.getData().entrances().forEach(this::onAddEntrance);
			this.blueprint.getData().getSchedules(ScheduleRegion.class).forEach(this::onAddSchedule);
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
		return this.blueprint;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.blueprint;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		this.renderOutskirts.setDisabled(playerOrbis.powers().getCurrentPower() != playerOrbis.powers().getEntrancePower());

		if (playerOrbis.getSelectedRegion() == this.blueprint && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getBlueprintPower())
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
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == layer)
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

	@Override
	public void onAddScheduleLayer(IScheduleLayer layer, int index)
	{

	}

	@Override
	public void onDataChanged()
	{

	}

	@Override
	public void onAddSchedule(ISchedule schedule)
	{
		if (schedule instanceof ScheduleRegion)
		{
			ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

			final Lock w = this.lock.writeLock();
			w.lock();

			try
			{
				RenderScheduleRegion r = new RenderScheduleRegion(this.blueprint, scheduleRegion);

				this.subRenderers.add(r);
			}
			finally
			{
				w.unlock();
			}
		}
	}

	@Override
	public void onRemoveSchedule(ISchedule schedule)
	{
		if (schedule instanceof ScheduleRegion)
		{
			ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

			final Lock w = this.lock.writeLock();
			w.lock();

			try
			{
				IWorldRenderer toRemove = null;

				for (IWorldRenderer renderer : this.subRenderers)
				{
					if (renderer.getRenderedObject() == scheduleRegion)
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

	@Override
	public void onAddEntrance(Entrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			Region r = new Region(entrance.getBounds());
			r.add(this.blueprint.getPos().getX(), this.blueprint.getPos().getY(), this.blueprint.getPos().getZ());
			RenderShape shape = new RenderShape(r);

			shape.useCustomColors = true;

			if (entrance.getBounds() instanceof IColored)
			{
				shape.colorBorder = ((IColored) entrance.getBounds()).getColor();
				shape.colorGrid = ((IColored) entrance.getBounds()).getColor();
			}

			this.subRenderers.add(shape);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveEntrance(Entrance entrance)
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
