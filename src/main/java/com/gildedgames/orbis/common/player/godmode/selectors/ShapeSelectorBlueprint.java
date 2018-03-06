package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddWorldObject;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShapeSelectorBlueprint implements IShapeSelector
{
	private final GodPowerBlueprint power;

	public ShapeSelectorBlueprint(final GodPowerBlueprint power)
	{
		this.power = power;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return this.power.getPlacingBlueprint() == null && held.isEmpty();
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		return !group.isIntersectingShapes(shape);
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (!world.isRemote)
		{
			final Blueprint blueprint = new Blueprint(world, selectedShape.getBoundingBox());

			playerOrbis.getWorldActionLog().track(world, new WorldActionAddWorldObject(blueprint));
		}
	}
}
