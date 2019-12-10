package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.client.godmode.GodPowerEntranceClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerEntrance;
import com.gildedgames.orbis.common.util.ColoredRegion;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddEntrance;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.data.framework.interfaces.EnumFacingMultiple;
import com.gildedgames.orbis.lib.data.pathway.Entrance;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.data.region.Region;
import com.gildedgames.orbis.lib.world.WorldObjectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class ShapeSelectorEntrance implements IShapeSelector
{
	private final GodPowerEntrance power;

	public ShapeSelectorEntrance(final GodPowerEntrance power)
	{
		this.power = power;
	}

	@Override
	public boolean canStartSelectingFrom(PlayerOrbis playerOrbis, BlockPos pos)
	{
		World world = playerOrbis.getWorld();

		Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, pos);

		if (b != null)
		{
			return !b.findIntersectingEntrance(pos).isPresent();
		}

		return false;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		return true;
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, shape);

		if (b != null)
		{
			return !b.findIntersectingEntrance(shape).isPresent();
		}

		return false;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (world.isRemote)
		{
			return;
		}

		Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, selectedShape);

		if (b != null)
		{
			Region r = new Region(selectedShape.getBoundingBox());

			r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

			ColoredRegion entrance = new ColoredRegion(r).setColor(GodPowerEntranceClient.SHAPE_COLOR);
			EntityPlayer entity = playerOrbis.getEntity();

			System.out.println(entity.rotationPitch);

			EnumFacingMultiple facing;

			if (entity.rotationPitch > 65 || entity.rotationPitch < -65) {
				facing = entity.rotationPitch > 65 ? EnumFacingMultiple.DOWN : EnumFacingMultiple.UP;
			} else {
				facing = EnumFacingMultiple.getFromMultiple(entity.getHorizontalFacing());
			}

			playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL)
					.apply(world, new WorldActionAddEntrance(b, new Entrance(entrance, null, facing)));
		}
	}

	@Override
	public void onSelectMultiple(PlayerOrbis playerOrbis, IShape selectedShape, World world, Set<BlockPos> multiplePositions)
	{

	}
}
