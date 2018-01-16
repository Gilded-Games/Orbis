package com.gildedgames.orbis.api.data.schedules;

public interface IScheduleLayerHolderListener
{

	void onChangeScheduleLayer(IScheduleLayer prevLayer, int prevIndex, IScheduleLayer newLayer, int newIndex);

}
