package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.api.world.WorldObjectUtils;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetSelectedRegion;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddSchedule;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShapeSelectorSelect implements IShapeSelector
{
	private final GodPowerSelect power;

	public ShapeSelectorSelect(final GodPowerSelect power)
	{
		this.power = power;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return held.isEmpty() || !(held.getItem() == ItemsOrbis.block_chunk || held.getItem() == ItemsOrbis.blueprint);
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		if (playerOrbis.powers().isScheduling())
		{
			Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, shape);

			if (b != null)
			{
				for (BlockPos pos : shape.getShapeData())
				{
					if (b.findIntersectingSchedule(pos) != null)
					{
						return false;
					}
				}

				return RegionHelp.contains(shape, b);
			}

			return false;
		}

		return true;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		if (world.isRemote)
		{
			return;
		}

		/*final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		if (held.getItem() instanceof ItemBlock)
		{
			final ItemStack offhand = playerOrbis.getEntity().getHeldItem(EnumHand.OFF_HAND);
			final BlockFilter filter;

			if (offhand.getItem() instanceof ItemBlock)
			{
				filter = new BlockFilter(BlockFilterHelper.getNewReplaceLayer(held, offhand));
			}
			else
			{
				filter = new BlockFilter(BlockFilterHelper.getNewFillLayer(held));
			}

			final CreationDataOrbis creationData = new CreationDataOrbis(world, playerOrbis.getEntity());

			creationData.schedules(playerOrbis.powers().isScheduling());

			filter.apply(selectedShape, creationData, true);

			return;
		}*/

		final WorldObjectManager manager = WorldObjectManager.get(world);

		if (playerOrbis.powers().isScheduling())
		{
			Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, selectedShape);

			if (b != null)
			{
				Region r = new Region(selectedShape.getBoundingBox());
				r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

				ScheduleRegion scheduleRegion = new ScheduleRegion("", r);

				playerOrbis.getWorldActionLog().track(world, new WorldActionAddSchedule(b, scheduleRegion, b.getCurrentScheduleLayerIndex()));
			}
		}
		else
		{
			final WorldShape region = new WorldShape(selectedShape, world);

			final int regionId = manager.fetchNextId();

			OrbisAPI.network().sendPacketToDimension(new PacketWorldObjectAdd(region, world.provider.getDimension(), regionId), world.provider.getDimension());

			if (this.power.getSelectedRegion() != null)
			{
				manager.removeObject(this.power.getSelectedRegionId());

				OrbisAPI.network()
						.sendPacketToDimension(new PacketWorldObjectRemove(this.power.getSelectedRegionId(), world.provider.getDimension()),
								world.provider.getDimension());
			}

			this.power.setSelectedRegion(region);
			this.power.setSelectedRegionId(regionId);

			OrbisAPI.network().sendPacketToPlayer(new PacketSetSelectedRegion(regionId), (EntityPlayerMP) playerOrbis.getEntity());
		}
	}
}
