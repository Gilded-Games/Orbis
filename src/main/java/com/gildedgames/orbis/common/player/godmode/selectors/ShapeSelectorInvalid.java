package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShapeSelectorInvalid implements IShapeSelector
{

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		return false;
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		return false;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{

	}
}
