package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.mc.NBT;

public interface IScheduleLayer extends NBT
{

	ScheduleDataType dataType();

	void listen(IScheduleLayerListener listener);

	boolean unlisten(IScheduleLayerListener listener);

	void setChoosesPerBlock(boolean choosesPerBlock);

	boolean choosesPerBlock();

	String getDisplayName();

	void setDisplayName(String displayName);

	float getEdgeNoise();

	void setEdgeNoise(float edgeNoise);

	IPositionRecord<BlockFilter> getDataRecord();

	void setDimensions(IDimensions dimensions);

}
