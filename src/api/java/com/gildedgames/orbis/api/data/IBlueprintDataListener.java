package com.gildedgames.orbis.api.data;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;

public interface IBlueprintDataListener
{
	void onRemoveScheduleLayer(IScheduleLayer layer, int index);

	void onAddScheduleLayer(IScheduleLayer layer, int index);

	void onDataChanged();

	void onAddEntrance(IRegion entrance);

	void onRemoveEntrance(IRegion entrance);
}
