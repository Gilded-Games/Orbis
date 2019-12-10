package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.data.region.IShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public interface IShapeSelector
{

	default boolean canStartSelectingFrom(PlayerOrbis playerOrbis, BlockPos pos)
	{
		return true;
	}

	boolean isSelectorActive(PlayerOrbis playerOrbis, World world);

	boolean canSelectShape(PlayerOrbis playerOrbis, IShape shape, World world);

	void onSelect(PlayerOrbis playerOrbis, IShape selectedShape, World world, @Nullable BlockPos start, @Nullable BlockPos end);

	void onSelectMultiple(PlayerOrbis playerOrbis, IShape selectedShape, World world, Set<BlockPos> multiplePositions);

}