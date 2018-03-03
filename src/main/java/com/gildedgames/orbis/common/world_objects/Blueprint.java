package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.blueprint.IBlueprintDataListener;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IColored;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderBlueprintEditing;
import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Blueprint extends BlueprintRegion implements IWorldObject, IColored, IBlueprintDataListener,
		IScheduleLayerHolder, IScheduleRecordListener
{
	private final List<IWorldObjectGroup> trackedGroups = Lists.newArrayList();

	private final List<IScheduleLayerHolderListener> listeners = Lists.newArrayList();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private World world;

	private IWorldRenderer renderer;

	private int currentScheduleLayer;

	private Blueprint(final World world)
	{
		super(world);
		this.world = world;
	}

	public Blueprint(final World world, final IRegion region)
	{
		super(region);
		this.world = world;
		this.setBounds(region);
		this.data.setWorldObjectParent(this);
	}

	public Blueprint(final World world, final BlockPos pos, final BlueprintData data)
	{
		super(pos, data);
		this.world = world;
		this.data.listen(this);
		this.data.setWorldObjectParent(this);
	}

	public Blueprint(final World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		super(pos, rotation, data);
		this.world = world;
		this.data.listen(this);
		this.data.setWorldObjectParent(this);
	}

	@Override
	public void listen(final IScheduleLayerHolderListener listener)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			if (!this.listeners.contains(listener))
			{
				this.listeners.add(listener);
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public boolean unlisten(final IScheduleLayerHolderListener listener)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			return this.listeners.remove(listener);
		}
		finally
		{
			w.unlock();
		}
	}

	public ISchedule findIntersectingSchedule(BlockPos pos)
	{
		if (this.getCurrentScheduleLayer() == null)
		{
			return null;
		}

		for (ISchedule r : this.getCurrentScheduleLayer().getScheduleRecord().getSchedules(ISchedule.class))
		{
			int minX = r.getBounds().getMin().getX() + this.getPos().getX();
			int minY = r.getBounds().getMin().getY() + this.getPos().getY();
			int minZ = r.getBounds().getMin().getZ() + this.getPos().getZ();

			int maxX = minX + r.getBounds().getWidth() - 1;
			int maxY = minY + r.getBounds().getHeight() - 1;
			int maxZ = minZ + r.getBounds().getLength() - 1;

			if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ)
			{
				return r;
			}
		}

		return null;
	}

	private void checkScheduleLayerExists()
	{
		boolean exists = this.getData().getScheduleLayers().containsKey(this.currentScheduleLayer);

		if (!exists && !this.getData().getScheduleLayers().isEmpty())
		{
			this.currentScheduleLayer = Collections.min(this.getData().getScheduleLayers().keySet());
		}
	}

	@Override
	public int getCurrentScheduleLayerIndex()
	{
		this.checkScheduleLayerExists();

		return this.currentScheduleLayer;
	}

	/**
	 * @param index The index of the current layer from the
	 *             	internal BlueprintData object.
	 */
	@Override
	public void setCurrentScheduleLayerIndex(final int index)
	{
		int oldIndex = this.currentScheduleLayer;
		IScheduleLayer oldLayer = this.getData().getScheduleLayers().get(oldIndex);

		if (oldLayer != null)
		{
			oldLayer.getScheduleRecord().unlisten(this);
		}

		this.currentScheduleLayer = index;

		this.getCurrentScheduleLayer().getScheduleRecord().listen(this);

		Lock w = this.lock.readLock();
		w.lock();

		try
		{
			this.listeners.forEach(l -> l.onChangeScheduleLayer(oldLayer, oldIndex, this.getCurrentScheduleLayer(), this.currentScheduleLayer));
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public IScheduleLayer getCurrentScheduleLayer()
	{
		this.checkScheduleLayerExists();

		return this.getData().getScheduleLayers().get(this.currentScheduleLayer);
	}

	@Override
	public BlueprintData getData()
	{
		return this.data;
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.min.hashCode());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		final boolean flag = super.equals(o);

		if (flag)
		{
			return true;
		}

		if (!(o instanceof Blueprint))
		{
			return false;
		}

		final Blueprint b = (Blueprint) o;

		if (this.getMin().getX() == b.getMin().getX() && this.getMax().getX() == b.getMax().getX() && this.getMin().getY() == b.getMin().getY()
				&& this.getMax().getY() == b.getMax().getY() && this.getMin().getZ() == b.getMin().getZ() && this.getMax().getZ() == b.getMax()
				.getZ() && this.data == b.data)
		{
			return this.getWorld().equals(b.getWorld());
		}

		return false;
	}

	@Override
	public void trackGroup(final IWorldObjectGroup group)
	{
		if (!this.trackedGroups.contains(group))
		{
			this.trackedGroups.add(group);
		}
	}

	@Override
	public void untrackGroup(final IWorldObjectGroup group)
	{
		this.trackedGroups.remove(group);
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public IShape getShape()
	{
		return this;
	}

	@Override
	public IWorldRenderer getRenderer()
	{
		if (OrbisCore.isClient() && this.renderer == null)
		{
			this.renderer = new RenderBlueprintEditing(this);
		}

		return this.renderer;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		super.write(tag);
		tag.setInteger("currentScheduleLayer", this.currentScheduleLayer);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		super.read(tag);
		this.data.listen(this);

		this.currentScheduleLayer = tag.getInteger("currentScheduleLayer");
		this.data.setWorldObjectParent(this);
	}

	@Override
	public int getColor()
	{
		return 0x99B6FF;
	}

	@Override
	public void onRemoveScheduleLayer(final IScheduleLayer layer, final int index)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onAddScheduleLayer(final IScheduleLayer layer, final int index)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onDataChanged()
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onAddSchedule(ISchedule schedule)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onRemoveSchedule(ISchedule schedule)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onAddEntrance(Entrance entrance)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onRemoveEntrance(Entrance entrance)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}
}
