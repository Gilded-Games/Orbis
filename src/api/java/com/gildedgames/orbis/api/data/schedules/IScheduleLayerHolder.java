package com.gildedgames.orbis.api.data.schedules;

public interface IScheduleLayerHolder
{

	int getCurrentScheduleLayerIndex();

	void setCurrentScheduleLayerIndex(final int index);

	IScheduleLayer getCurrentScheduleLayer();

	void listen(IScheduleLayerHolderListener listener);

	boolean unlisten(IScheduleLayerHolderListener listener);

}
