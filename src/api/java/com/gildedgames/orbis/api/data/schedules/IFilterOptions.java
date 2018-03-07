package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.util.mc.NBT;

public interface IFilterOptions extends NBT
{

	IFilterOptions setChoosesPerBlock(boolean choosesPerBlock);

	boolean choosesPerBlock();

	float getEdgeNoise();

	IFilterOptions setEdgeNoise(float edgeNoise);

}
