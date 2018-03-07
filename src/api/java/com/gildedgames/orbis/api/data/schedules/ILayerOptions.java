package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.util.mc.NBT;

public interface ILayerOptions extends NBT
{

	ILayerOptions setChoosesPerBlock(boolean choosesPerBlock);

	boolean choosesPerBlock();

	float getEdgeNoise();

	ILayerOptions setEdgeNoise(float edgeNoise);

}
