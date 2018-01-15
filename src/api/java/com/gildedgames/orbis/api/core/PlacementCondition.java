package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.processing.IBlockAccessExtended;
import net.minecraft.util.math.BlockPos;

public interface PlacementCondition
{

	boolean canPlace(BlueprintData data, IBlockAccessExtended world, BlockPos placedAt, BlockData block, BlockPos pos);

	/** Should return true by default **/
	boolean canPlaceCheckAll(BlueprintData data, IBlockAccessExtended world, BlockPos placedAt, BlockDataContainer blocks);

}