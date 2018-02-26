package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.schedules.ISchedule;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;

public interface IBlueprintDataListener
{
	void onRemoveScheduleLayer(IScheduleLayer layer, int index);

	void onAddScheduleLayer(IScheduleLayer layer, int index);

	void onDataChanged();

	void onAddSchedule(ISchedule schedule);

	void onRemoveSchedule(ISchedule schedule);

	void onAddEntrance(Entrance entrance);

	void onRemoveEntrance(Entrance entrance);
}
