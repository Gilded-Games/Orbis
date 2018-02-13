package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.IBlueprintDataListener;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolder;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolderListener;
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

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Blueprint extends BlueprintRegion implements IWorldObject, IColored, IBlueprintDataListener,
		IScheduleLayerHolder
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
	}

	public Blueprint(final World world, final BlockPos pos, final BlueprintData data)
	{
		super(pos, data);
		this.world = world;
		this.data.listen(this);
	}

	public Blueprint(final World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		super(pos, rotation, data);
		this.world = world;
		this.data.listen(this);
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

	@Override
	public int getCurrentScheduleLayerIndex()
	{
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

		this.currentScheduleLayer = index;

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
