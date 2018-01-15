package com.gildedgames.orbis.api.data;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.data.management.impl.DataMetadata;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IRotateable;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.IPositionRecordListener;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerListener;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;

public class BlueprintData implements IDimensions, NBT, IData, IScheduleLayerListener, IPositionRecordListener<BlockFilter>
{
	private final List<IBlueprintDataListener> listeners = Lists.newArrayList();

	private IDataMetadata metadata;

	private BlockDataContainer dataContainer;

	private Map<Integer, IScheduleLayer> scheduleLayers = Maps.newHashMap();

	private BlueprintData()
	{
		this.metadata = new DataMetadata();
	}

	public BlueprintData(final IRegion region)
	{
		this();

		this.dataContainer = new BlockDataContainer(region);
	}

	public BlueprintData(final BlockDataContainer container)
	{
		this();

		this.dataContainer = container;
	}

	public void listen(final IBlueprintDataListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	public BlockDataContainer getBlockDataContainer()
	{
		return this.dataContainer;
	}

	public Map<Integer, IScheduleLayer> getScheduleLayers()
	{
		return this.scheduleLayers;
	}

	public void setScheduleLayer(final int index, final IScheduleLayer layer)
	{
		this.listeners.forEach(o -> o.onAddScheduleLayer(layer, this.scheduleLayers.size()));

		this.scheduleLayers.put(index, layer);

		layer.listen(this);
	}

	public void addScheduleLayer(final IScheduleLayer layer)
	{
		this.setScheduleLayer(this.scheduleLayers.size(), layer);
	}

	public boolean removeScheduleLayer(final int index)
	{
		final boolean removed = this.scheduleLayers.get(index) != null;

		final IScheduleLayer layer = this.scheduleLayers.remove(index);

		this.listeners.forEach(o -> o.onRemoveScheduleLayer(layer, index));

		layer.unlisten(this);

		return removed;
	}

	public int getIndexOfScheduleLayer(final IScheduleLayer layer)
	{
		for (int i = 0; i < this.scheduleLayers.size(); i++)
		{
			final IScheduleLayer l = this.scheduleLayers.get(i);

			if (layer.equals(l))
			{
				return i;
			}
		}

		return -1;
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
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.dataContainer = funnel.get("dataContainer");
		this.metadata = funnel.get("metadata");
		this.scheduleLayers = funnel.getIntMap("scheduleLayers");

		this.scheduleLayers.values().forEach(l -> l.setDimensions(this));

		this.scheduleLayers.values().forEach(l -> l.listen(this));
		this.scheduleLayers.values().forEach(l -> l.getDataRecord().listen(this));
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

}
