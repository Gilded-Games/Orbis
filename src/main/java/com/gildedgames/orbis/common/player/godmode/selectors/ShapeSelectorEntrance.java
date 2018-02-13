package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayUtil;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.godmode.GodPowerEntranceClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerEntrance;
import com.gildedgames.orbis.common.util.ColoredRegion;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.item.ItemStack;
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
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		final BlockPos endPos = RaytraceHelp.doOrbisRaytrace(playerOrbis);

		Blueprint b = group.getIntersectingShape(Blueprint.class, endPos);

		if (b == null)
		{
			return false;
		}

		boolean north = endPos.getX() == b.getMin().getX();
		boolean south = endPos.getX() == b.getMax().getX();
		boolean east = endPos.getZ() == b.getMax().getZ();
		boolean west = endPos.getZ() == b.getMin().getZ();
		boolean up = endPos.getY() == b.getMax().getY();
		boolean down = endPos.getY() == b.getMin().getY();

		return (north || south || east || west || up || down) && (held.isEmpty() || !(held.getItem() == ItemsOrbis.block_chunk
				|| held.getItem() == ItemsOrbis.blueprint));
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		Blueprint b = group.getIntersectingShape(Blueprint.class, shape);

		if (b != null)
		{
			IRegion bb = shape.getBoundingBox();

			if (bb.getLength() == 1 || bb.getWidth() == 1)
			{
				boolean north = bb.getMin().getX() == b.getMin().getX() && bb.getWidth() == 1;
				boolean south = bb.getMax().getX() == b.getMax().getX() && bb.getWidth() == 1;
				boolean east = bb.getMax().getZ() == b.getMax().getZ() && bb.getLength() == 1;
				boolean west = bb.getMin().getZ() == b.getMin().getZ() && bb.getLength() == 1;
				boolean up = bb.getMin().getY() == b.getMax().getY() && bb.getHeight() == 1;
				boolean down = bb.getMin().getY() == b.getMin().getY() && bb.getHeight() == 1;

				if (north || south || east || west || up || down)
				{
					return true;
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

		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		Blueprint b = group.getIntersectingShape(Blueprint.class, selectedShape);

		if (b != null)
		{
			Region r = new Region(selectedShape.getBoundingBox());

			EnumFacing[] facings = PathwayUtil.sidesOfConnection(b, r);

			r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

			ColoredRegion entrance = new ColoredRegion(r).setColor(GodPowerEntranceClient.SHAPE_COLOR);

			b.getData().addEntrance(new Entrance(entrance, null, facings));
		}
	}
}
