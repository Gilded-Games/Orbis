package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

		return held.getItem() instanceof ItemBlock || (this.canSelectWithoutItems && held.isEmpty()) || held.getItem() instanceof ItemBucket;
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		return group.getIntersectingShapes(Blueprint.class, shape).size() == 1 || !playerOrbis.powers().isScheduling();
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (world.isRemote)
		{
			return;
		}

		final BlockFilter filter = this.filterSupplier.apply(playerOrbis.getEntity());

		final ICreationData creationData = new CreationDataOrbis(world, playerOrbis.getEntity());

		creationData.schedules(playerOrbis.powers().isScheduling());

		filter.apply(selectedShape, world, creationData);
	}
}
