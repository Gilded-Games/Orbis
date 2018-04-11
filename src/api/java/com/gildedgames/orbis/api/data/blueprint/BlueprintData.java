package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.PlacedEntity;
import com.gildedgames.orbis.api.data.IDataHolder;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.data.management.impl.DataMetadata;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.BlueprintHelper;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldObjectChild;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlueprintData
		implements IDimensions, IData, IScheduleLayerListener, IPositionRecordListener<BlockFilter>, IWorldObjectChild, IDataHolder<BlueprintData>
{
	public static final String EXTENSION = "blueprint";

	private final List<IBlueprintDataListener> listeners = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private IDataMetadata metadata;

	private BlockDataContainer dataContainer;

	private LinkedHashMap<Integer, IScheduleLayer> scheduleLayers = Maps.newLinkedHashMap();

	private List<Entrance> entrances = Lists.newArrayList();

	private IWorldObject worldObjectParent;

	private BlueprintData()
	{
		this.metadata = new DataMetadata();
	}

	public BlueprintData(final IRegion region)
	{
		this();

		this.dataContainer = new BlockDataContainer(region);
		this.addScheduleLayer(new ScheduleLayer("Default Layer", this));
	}

	public BlueprintData(final BlockDataContainer container)
	{
		this();

		this.dataContainer = container;
		this.addScheduleLayer(new ScheduleLayer("Default Layer", this));
	}

	public static void spawnEntities(DataPrimer primer, BlueprintData data, BlockPos pos)
	{
		for (IScheduleLayer layer : data.getScheduleLayers().values())
		{
			for (ScheduleRegion s : layer.getScheduleRecord().getSchedules(ScheduleRegion.class))
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
	}

	public int getEntranceId(Entrance entrance)
	{
		int i = 0;
		for (Entrance e : this.entrances)
		{
			if (e == entrance)
			{
				return i;
			}

			i++;
		}

		return -1;
	}

	public void listen(final IBlueprintDataListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	@Override
	public IWorldObject getWorldObjectParent()
	{
		return this.worldObjectParent;
	}

	@Override
	public void setWorldObjectParent(IWorldObject parent)
	{
		this.worldObjectParent = parent;

		this.scheduleLayers.values().forEach(s -> s.setWorldObjectParent(this.worldObjectParent));
		this.entrances.forEach(e -> e.setWorldObjectParent(this.worldObjectParent));
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

			entrance.setWorldObjectParent(this.worldObjectParent);

			this.entrances.add(entrance);

			this.listeners.forEach(o -> o.onAddEntrance(entrance));
		}
		finally
		{
			w.unlock();
		}
	}

	public boolean removeEntrance(int id)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			Entrance entrance = this.entrances.remove(id);

			if (entrance != null)
			{
				this.listeners.forEach(o -> o.onRemoveEntrance(entrance));
			}

			return entrance != null;
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

		layer.setWorldObjectParent(this.worldObjectParent);
		layer.setLayerId(index);

		this.scheduleLayers.put(index, layer);

		layer.listen(this);
	}

	public int findNextAvailableId()
	{
		int i = 0;

		while (this.scheduleLayers.containsKey(i))
		{
			i++;
		}

		return i;
	}

	public int addScheduleLayer(final IScheduleLayer layer)
	{
		int id = this.findNextAvailableId();

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

		funnel.set("metadata", this.metadata);
		funnel.set("dataContainer", this.dataContainer);
		funnel.setIntMap("scheduleLayers", this.scheduleLayers);
		funnel.setList("entrances", this.entrances);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.metadata = funnel.get("metadata");
		this.dataContainer = funnel.get("dataContainer");
		this.scheduleLayers = Maps.newLinkedHashMap(funnel.getIntMap("scheduleLayers"));

		this.scheduleLayers.values().forEach(l -> l.setDimensions(this));

		this.scheduleLayers.values().forEach(l -> l.listen(this));
		this.scheduleLayers.values().forEach(l -> l.getFilterRecord().listen(this));

		this.entrances = funnel.getList("entrances");
	}

	@Override
	public void preSaveToDisk(final IWorldObject object)
	{
		if (object instanceof IShape)
		{
			final IShape shape = (IShape) object;

			this.dataContainer = BlueprintHelper.fetchBlocksInside(shape, object.getWorld());
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

	@Override
	public void readMetadataOnly(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.metadata = funnel.get("metadata");
	}

	@Override
	public BlueprintData get(World world, Random random)
	{
		return this;
	}

	@Override
	public int getLargestHeight()
	{
		return this.getHeight();
	}

	@Override
	public int getLargestWidth()
	{
		return this.getWidth();
	}

	@Override
	public int getLargestLength()
	{
		return this.getLength();
	}
}
