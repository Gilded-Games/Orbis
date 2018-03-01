package com.gildedgames.orbis.api.data.schedules;

public interface IScheduleRecordListener
{

	void onAddSchedule(ISchedule schedule);

	void onRemoveSchedule(ISchedule schedule);

}
