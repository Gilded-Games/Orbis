package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerPathway;
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

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		GodPowerPathway p = playerOrbis.powers().getPathwayPower();

		p.processPathway(playerOrbis, start, end);

		primer.create(null, p.getInitialNode().getData(),
				new CreationData(world, playerOrbis.getEntity()).pos(p.getInitialNode().getMin()).rotation(p.getInitialNode().getRotation()).placesAir(true));

		for (PathwayNode n : p.getStepAStar().currentState().fullPath())
		{
			primer.create(null, n.getData(), new CreationData(world, playerOrbis.getEntity()).pos(n.getMin()).rotation(n.getRotation()).placesAir(true));
		}
	}
}
