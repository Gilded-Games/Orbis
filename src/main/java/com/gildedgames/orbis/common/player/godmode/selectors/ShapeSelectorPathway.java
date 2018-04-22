package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerPathway;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionPathway;
import com.gildedgames.orbis_api.data.region.IShape;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShapeSelectorPathway implements IShapeSelector
{
	private final GodPowerPathway power;

	public ShapeSelectorPathway(final GodPowerPathway power)
	{
		this.power = power;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty() && (held.getItem() == ItemsOrbis.blueprint_palette || held.getItem() == ItemsOrbis.blueprint);
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		return true;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (world.isRemote)
		{
			return;
		}

		playerOrbis.getWorldActionLog().track(world, new WorldActionPathway(start, end));
	}
}
