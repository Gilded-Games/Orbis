package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IWorldObject;

public interface ISchedule extends NBT
{

	IWorldObject getWorldObjectParent();

	void setWorldObjectParent(IWorldObject parent);

	BlueprintData getParent();

	void setParent(BlueprintData parent);

	IRegion getBounds();

}
