package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemBlockPalette;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionFilter;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionFilterMultiple;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.block.BlockFilter;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.world.WorldObjectUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.Function;

public class ShapeSelectorFilter implements IShapeSelector
{

	private final Function<EntityPlayer, BlockFilter> filterSupplier;

	private final boolean canSelectWithoutItems;

	public ShapeSelectorFilter(final Function<EntityPlayer, BlockFilter> filterSupplier, final boolean canSelectWithoutItems)
	{
		this.filterSupplier = filterSupplier;
		this.canSelectWithoutItems = canSelectWithoutItems;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return held.getItem() instanceof ItemBlock || (this.canSelectWithoutItems && held.isEmpty()) || held.getItem() instanceof ItemBucket || held
				.getItem() instanceof ItemBlockPalette;
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		return WorldObjectUtils.getIntersectingShapes(world, Blueprint.class, shape).size() == 1 || !playerOrbis.powers().isScheduling();
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (world.isRemote)
		{
			return;
		}

		final BlockFilter filter = this.filterSupplier.apply(playerOrbis.getEntity());

		playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL).apply(world, new WorldActionFilter(selectedShape, filter, playerOrbis.powers().isScheduling()));
	}

	@Override
	public void onSelectMultiple(PlayerOrbis playerOrbis, IShape selectedShape, World world, Set<BlockPos> multiplePositions)
	{
		if (world.isRemote)
		{
			return;
		}

		final BlockFilter filter = this.filterSupplier.apply(playerOrbis.getEntity());

		playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL)
				.apply(world, new WorldActionFilterMultiple(selectedShape, filter, playerOrbis.powers().isScheduling(), multiplePositions));
	}
}
