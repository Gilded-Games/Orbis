package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis_api.data.framework.generation.searching.PathwayUtil;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import com.gildedgames.orbis.client.godmode.GodPowerEntranceClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerEntrance;
import com.gildedgames.orbis.common.util.ColoredRegion;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddEntrance;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
			boolean up = pos.getY() == b.getMax().getY();
			boolean down = pos.getY() == b.getMin().getY();
			boolean north = pos.getX() == b.getMin().getX();
			boolean south = pos.getX() == b.getMax().getX();
			boolean east = pos.getZ() == b.getMax().getZ();
			boolean west = pos.getZ() == b.getMin().getZ();

			if (up || down || north || south || east || west)
			{
				return b.findIntersectingEntrance(pos) == null;
			}
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
			IRegion bb = shape.getBoundingBox();

			boolean up = bb.getMin().getY() == b.getMax().getY() && bb.getHeight() == 1;
			boolean down = bb.getMin().getY() == b.getMin().getY() && bb.getHeight() == 1;

			if (bb.getLength() == 1 || bb.getWidth() == 1 || up || down)
			{
				boolean north = bb.getMin().getX() == b.getMin().getX() && bb.getWidth() == 1;
				boolean south = bb.getMax().getX() == b.getMax().getX() && bb.getWidth() == 1;
				boolean east = bb.getMax().getZ() == b.getMax().getZ() && bb.getLength() == 1;
				boolean west = bb.getMin().getZ() == b.getMin().getZ() && bb.getLength() == 1;

				if (north || south || east || west || up || down)
				{
					return b.findIntersectingEntrance(shape) == null;
				}
			}
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

			EnumFacing[] facings = PathwayUtil.sidesOfConnection(b, r);

			r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

			ColoredRegion entrance = new ColoredRegion(r).setColor(GodPowerEntranceClient.SHAPE_COLOR);

			playerOrbis.getWorldActionLog().track(world, new WorldActionAddEntrance(b, new Entrance(entrance, null, facings)));
		}
	}
}
