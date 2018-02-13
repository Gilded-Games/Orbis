package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IShapeSelector
{

	boolean isSelectorActive(PlayerOrbis playerOrbis, World world);

	boolean canSelectShape(PlayerOrbis playerOrbis, IShape shape, World world);

	void onSelect(PlayerOrbis playerOrbis, IShape selectedShape, World world, @Nullable BlockPos start, @Nullable BlockPos end);

}