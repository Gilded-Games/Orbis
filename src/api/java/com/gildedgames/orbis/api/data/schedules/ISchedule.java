package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IWorldObjectChild;

public interface ISchedule extends NBT, IWorldObjectChild
{

	String getTriggerId();

	void setTriggerId(String triggerId);

	IScheduleRecord getParent();

	void setParent(IScheduleRecord parent);

	IRegion getBounds();

}
