package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ScheduleLayer implements IScheduleLayer
{
	private final List<IScheduleLayerListener> listeners = Lists.newArrayList();

	private String displayName;

	private IDimensions dimensions;

	private IPositionRecord<BlockFilter> positionRecord;

	private ScheduleDataType dataType;

	private ScheduleLayer()
	{

	}

	public ScheduleLayer(final String displayName, final IDimensions dimensions, final ScheduleDataType dataType)
	{
		this.displayName = displayName;
		this.dimensions = dimensions;
		this.dataType = dataType;

		this.positionRecord = new FilterRecord(this.dimensions.getWidth(), this.dimensions.getHeight(), this.dimensions.getLength());
	}

	@Override
	public ScheduleDataType dataType()
	{
		return this.dataType;
	}

	@Override
	public void listen(final IScheduleLayerListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	@Override
	public boolean unlisten(final IScheduleLayerListener listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public String displayName()
	{
		return this.displayName;
	}

	@Override
	public IPositionRecord<BlockFilter> getDataRecord()
	{
		return this.positionRecord;
	}

	@Override
	public void setDimensions(final IDimensions dimensions)
	{
		this.dimensions = dimensions;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("displayName", this.displayName);
		funnel.set("positionRecord", this.positionRecord);
		tag.setInteger("dataType", this.dataType.ordinal());
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.displayName = tag.getString("displayName");
		this.positionRecord = funnel.get("positionRecord");
		this.dataType = ScheduleDataType.values()[tag.getInteger("dataType")];
	}
}
