package com.gildedgames.orbis.api.data.region;

import net.minecraft.util.math.BlockPos;

public interface IRegion extends IDimensions, IShape
{

	BlockPos getMin();

	BlockPos getMax();

}
