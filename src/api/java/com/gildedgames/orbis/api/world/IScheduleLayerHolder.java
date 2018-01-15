package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;

public interface IScheduleLayerHolder
{

	int getCurrentScheduleLayerIndex();

	void setCurrentScheduleLayerIndex(final int index);

	IScheduleLayer getCurrentScheduleLayer();

}
