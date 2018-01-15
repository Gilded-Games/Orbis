package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.mc.NBT;

public interface IScheduleLayer extends NBT
{

	ScheduleDataType dataType();

	void listen(IScheduleLayerListener listener);

	boolean unlisten(IScheduleLayerListener listener);

	String displayName();

	IPositionRecord getDataRecord();

	void setDimensions(IDimensions dimensions);

}
