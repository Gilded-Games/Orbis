package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.PlacedEntity;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.data.management.impl.DataMetadata;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IRotateable;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.ObjectFilter;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlueprintData implements IDimensions, IData, IScheduleLayerListener, IPositionRecordListener<BlockFilter>
{
	private final List<IBlueprintDataListener> listeners = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private IDataMetadata metadata;

	private BlockDataContainer dataContainer;

	private LinkedHashMap<Integer, IScheduleLayer> scheduleLayers = Maps.newLinkedHashMap();

	private List<Entrance> entrances = Lists.newArrayList();

	private Map<Integer, ISchedule> schedules = Maps.newHashMap();

	private IWorldObject worldObjectParent;

	private BlueprintData()
	{
		this.metadata = new DataMetadata();
	}

	public BlueprintData(final IRegion region)
	{
		this();

		this.dataContainer = new BlockDataContainer(region);
		this.addScheduleLayer(new ScheduleLayer("Default Layer", this, ScheduleDataType.DATA));
	}

	public BlueprintData(final BlockDataContainer container)
	{
		this();

		this.dataContainer = container;
		this.addScheduleLayer(new ScheduleLayer("Default Layer", this, ScheduleDataType.DATA));
	}

	public static void spawnEntities(DataPrimer primer, BlueprintData data, BlockPos pos)
	{
		for (ScheduleRegion s : data.getSchedules(ScheduleRegion.class))
		{
			for (int i = 0; i < s.getSpawnEggsInventory().getSizeInventory(); i++)
			{
				ItemStack stack = s.getSpawnEggsInventory().getStackInSlot(i);

				if (stack.getItem() instanceof ItemMonsterPlacer)
				{
					BlockPos p = pos.add(s.getBounds().getMin())
							.add(primer.getWorld().rand.nextInt(s.getBounds().getWidth()), 0, primer.getWorld().rand.nextInt(s.getBounds().getHeight()));

					PlacedEntity placedEntity = new PlacedEntity(stack, p);

					placedEntity.spawn(primer);
				}
			}
		}
	}

	public void listen(final IBlueprintDataListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	public IWorldObject getWorldObjectParent()
	{
		return this.worldObjectParent;
	}

	public void setWorldObjectParent(IWorldObject parent)
	{
		this.worldObjectParent = parent;

		this.schedules.values().forEach(s -> s.setParent(this));
		this.schedules.values().forEach(s -> s.setWorldObjectParent(this.worldObjectParent));
	}

	public <T extends ISchedule> List<T> getSchedules(Class<T> clazz)
	{
		return ObjectFilter.getTypesFrom(this.schedules.values(), clazz);
	}

	public ScheduleRegion getScheduleFromTriggerID(String triggerId)
	{
		for (ScheduleRegion s : this.getSchedules(ScheduleRegion.class))
		{
			if (s.getTriggerID().equals(triggerId))
			{
				return s;
			}
		}

		return null;
	}

	public void setSchedule(int id, ISchedule schedule)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			if (schedule instanceof ScheduleRegion)
			{
				ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

				boolean intersects = false;

				for (ScheduleRegion s : this.getSchedules(ScheduleRegion.class))
				{
					if (RegionHelp.intersects(scheduleRegion.getBounds(), s.getBounds()))
					{
						intersects = true;
						break;
					}
				}

				if (intersects)
				{
					throw new IllegalArgumentException("Schedule regions cannot intersect with other schedule regions in the Blueprint");
				}
			}

			schedule.setParent(this);
			schedule.setWorldObjectParent(this.worldObjectParent);

			this.schedules.put(id, schedule);

			this.listeners.forEach(o -> o.onAddSchedule(schedule));
		}
		finally
		{
			w.unlock();
		}
	}

	public int addSchedule(final ISchedule schedule)
	{
		int id = this.schedules.size();

		this.setSchedule(id, schedule);

		return id;
	}

	public void removeSchedule(int id)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			ISchedule schedule = this.schedules.remove(id);

			this.listeners.forEach(o -> o.onRemoveSchedule(schedule));
		}
		finally
		{
			w.unlock();
		}
	}

	public void addEntrance(Entrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			BlockPos ePos = entrance.getBounds().getMin();
			boolean properEntrance = ePos.getX() == 0 || ePos.getX() == this.getWidth() - 1 ||
					ePos.getY() == 0 || ePos.getY() == this.getHeight() - 1 ||
					ePos.getZ() == 0 || ePos.getZ() == this.getLength() - 1;
			if (!properEntrance)
			{
				throw new IllegalArgumentException("Entrance can only be placed on the edges of blueprints");
			}

			this.entrances.add(entrance);

			this.listeners.forEach(o -> o.onAddEntrance(entrance));
		}
		finally
		{
			w.unlock();
		}
	}

	public boolean removeEntrance(Entrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			boolean flag = this.entrances.remove(entrance);

			if (flag)
			{
				this.listeners.forEach(o -> o.onRemoveEntrance(entrance));
			}

			return flag;
		}
		finally
		{
			w.unlock();
		}
	}

	public BlockDataContainer getBlockDataContainer()
	{
		return this.dataContainer;
	}

	public LinkedHashMap<Integer, IScheduleLayer> getScheduleLayers()
	{
		return this.scheduleLayers;
	}

	public void setScheduleLayer(final int index, final IScheduleLayer layer)
	{
		this.listeners.forEach(o -> o.onAddScheduleLayer(layer, this.scheduleLayers.size()));

		this.scheduleLayers.put(index, layer);

		layer.listen(this);
	}

	public int addScheduleLayer(final IScheduleLayer layer)
	{
		int id = this.scheduleLayers.size();

		this.setScheduleLayer(id, layer);

		return id;
	}

	public boolean removeScheduleLayer(final int index)
	{
		final boolean removed = this.scheduleLayers.get(index) != null;

		final IScheduleLayer layer = this.scheduleLayers.remove(index);

		this.listeners.forEach(o -> o.onRemoveScheduleLayer(layer, index));

		layer.unlisten(this);

		return removed;
	}

	public <T extends ISchedule> T getSchedule(int id, Class<T> clazz)
	{
		return (T) this.schedules.get(id);
	}

	public ISchedule getSchedule(int id)
	{
		return this.schedules.get(id);
	}

	public int getScheduleId(final ISchedule schedule)
	{
		for (Map.Entry<Integer, ISchedule> entry : this.schedules.entrySet())
		{
			int i = entry.getKey();
			final ISchedule s = entry.getValue();

			if (schedule.equals(s))
			{
				return i;
			}
		}

		return -1;
	}

	public IScheduleLayer getScheduleLayer(int id)
	{
		return this.scheduleLayers.get(id);
	}

	public int getScheduleLayerId(final IScheduleLayer layer)
	{
		for (Map.Entry<Integer, IScheduleLayer> entry : this.scheduleLayers.entrySet())
		{
			int i = entry.getKey();
			final IScheduleLayer s = entry.getValue();

			if (layer.equals(s))
			{
				return i;
			}
		}

		return -1;
	}

	public List<Entrance> entrances()
	{
		return this.entrances;
	}

	@Override
	public int getWidth()
	{
		return this.dataContainer.getWidth();
	}

	@Override
	public int getHeight()
	{
		return this.dataContainer.getHeight();
	}

	@Override
	public int getLength()
	{
		return this.dataContainer.getLength();
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.metadata.getIdentifier());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj instanceof BlueprintData)
		{
			final BlueprintData o = (BlueprintData) obj;

			final EqualsBuilder builder = new EqualsBuilder();

			builder.append(this.metadata.getIdentifier(), o.metadata.getIdentifier());

			return builder.isEquals();
		}

		return false;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("dataContainer", this.dataContainer);
		funnel.set("metadata", this.metadata);
		funnel.setIntMap("scheduleLayers", this.scheduleLayers);
		funnel.setList("entrances", this.entrances);
		funnel.setIntMap("schedules", this.schedules);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.dataContainer = funnel.get("dataContainer");
		this.metadata = funnel.get("metadata");
		this.scheduleLayers = Maps.newLinkedHashMap(funnel.getIntMap("scheduleLayers"));

		this.scheduleLayers.values().forEach(l -> l.setDimensions(this));

		this.scheduleLayers.values().forEach(l -> l.listen(this));
		this.scheduleLayers.values().forEach(l -> l.getDataRecord().listen(this));

		this.entrances = funnel.getList("entrances");

		this.schedules = funnel.getIntMap("schedules");

		this.schedules.values().forEach(s -> s.setParent(this));
	}

	public void fetchBlocksInside(final IShape shape, final World world, final Rotation rotation)
	{
		final BlockDataContainer container = new BlockDataContainer(shape.getBoundingBox());

		final BlockPos min = shape.getBoundingBox().getMin();

		for (final BlockPos pos : shape.createShapeData())
		{
			final BlockData blockData = new BlockData().getDataFrom(pos, world);

			final BlockPos translated = pos.add(-min.getX(), -min.getY(), -min.getZ());

			container.set(blockData, translated);
		}

		this.dataContainer = container;
	}

	@Override
	public void preSaveToDisk(final IWorldObject object)
	{
		if (object instanceof IShape)
		{
			final IShape shape = (IShape) object;
			Rotation rotation = Rotation.NONE;

			if (object instanceof IRotateable)
			{
				final IRotateable rotateable = (IRotateable) object;

				rotation = rotateable.getRotation();
			}

			this.fetchBlocksInside(shape, object.getWorld(), rotation);
		}
	}

	@Override
	public String getFileExtension()
	{
		return "blueprint";
	}

	@Override
	public IDataMetadata getMetadata()
	{
		return this.metadata;
	}

	@Override
	public IData clone()
	{
		final BlueprintData data = new BlueprintData();

		final NBTTagCompound tag = new NBTTagCompound();

		this.write(tag);

		data.read(tag);

		return data;
	}

	@Override
	public void onMarkPos(final BlockFilter filter, final int x, final int y, final int z)
	{
		this.listeners.forEach(IBlueprintDataListener::onDataChanged);
	}

	@Override
	public void onUnmarkPos(final int x, final int y, final int z)
	{
		this.listeners.forEach(IBlueprintDataListener::onDataChanged);
	}

	@Override
	public void onSetDimensions(final IDimensions dimensions)
	{

	}

	public void markDirty()
	{
		this.listeners.forEach(IBlueprintDataListener::onDataChanged);
	}

}
