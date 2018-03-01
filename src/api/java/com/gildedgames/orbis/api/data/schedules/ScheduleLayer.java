package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ScheduleLayer implements IScheduleLayer
{
	private final List<IScheduleLayerListener> listeners = Lists.newArrayList();

	private String displayName;

	private IDimensions dimensions;

	private IPositionRecord<BlockFilter> positionRecord;

	private IScheduleRecord scheduleRecord = new ScheduleRecord();

	private float edgeNoise;

	private boolean choosesPerBlock = true;

	private IWorldObject worldObjectParent;

	private int layerId;

	private ScheduleLayer()
	{
		this.scheduleRecord.setParent(this);
	}

	public ScheduleLayer(final String displayName, final IDimensions dimensions)
	{
		this.displayName = displayName;
		this.dimensions = dimensions;

		this.positionRecord = new FilterRecord(this.dimensions.getWidth(), this.dimensions.getHeight(), this.dimensions.getLength());

		this.scheduleRecord.setParent(this);
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
	public void setChoosesPerBlock(boolean choosesPerBlock)
	{
		this.choosesPerBlock = choosesPerBlock;
	}

	@Override
	public boolean choosesPerBlock()
	{
		return this.choosesPerBlock;
	}

	@Override
	public String getDisplayName()
	{
		return this.displayName;
	}

	@Override
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public float getEdgeNoise()
	{
		return this.edgeNoise;
	}

	@Override
	public void setEdgeNoise(float edgeNoise)
	{
		this.edgeNoise = edgeNoise;
	}

	@Override
	public IPositionRecord<BlockFilter> getFilterRecord()
	{
		return this.positionRecord;
	}

	@Override
	public IScheduleRecord getScheduleRecord()
	{
		return this.scheduleRecord;
	}

	@Override
	public void setDimensions(final IDimensions dimensions)
	{
		this.dimensions = dimensions;
	}

	@Override
	public int getLayerId()
	{
		return this.layerId;
	}

	@Override
	public void setLayerId(int layerId)
	{
		this.layerId = layerId;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("positionRecord", this.positionRecord);
		funnel.set("scheduleRecord", this.scheduleRecord);

		tag.setString("displayName", this.displayName);
		tag.setFloat("edgeNoise", this.edgeNoise);
		tag.setBoolean("choosesPerBlock", this.choosesPerBlock);

		tag.setInteger("layerId", this.layerId);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.positionRecord = funnel.get("positionRecord");

		IScheduleRecord record = funnel.get("scheduleRecord");

		if (record != null)
		{
			this.scheduleRecord = record;
		}

		this.displayName = tag.getString("displayName");
		this.edgeNoise = tag.getFloat("edgeNoise");
		this.choosesPerBlock = tag.getBoolean("choosesPerBlock");

		this.layerId = tag.getInteger("layerId");

		if (this.scheduleRecord != null)
		{
			this.scheduleRecord.setParent(this);
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

		this.scheduleRecord.setWorldObjectParent(parent);
	}

}
