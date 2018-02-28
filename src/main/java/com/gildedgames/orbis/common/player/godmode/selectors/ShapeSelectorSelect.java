package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetSelectedRegion;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketAddSchedule;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		if (playerOrbis.powers().isScheduling())
		{
			Blueprint b = group.getIntersectingShape(Blueprint.class, shape);

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

		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

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

			filter.apply(selectedShape, creationData);

			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		if (playerOrbis.powers().isScheduling())
		{
			Blueprint b = group.getIntersectingShape(Blueprint.class, selectedShape);

			if (b != null)
			{
				Region r = new Region(selectedShape.getBoundingBox());
				r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

				ScheduleRegion scheduleRegion = new ScheduleRegion("", r);

				if (world.getMinecraftServer().isDedicatedServer())
				{
					NetworkingOrbis.sendPacketToDimension(new PacketAddSchedule(b, scheduleRegion), world.provider.getDimension());
				}
				else
				{
					b.getData().addSchedule(scheduleRegion);
				}
			}
		}
		else
		{
			final WorldShape region = new WorldShape(selectedShape, world);

			final int regionId = group.addObject(region);

			if (world.getMinecraftServer().isDedicatedServer())
			{
				NetworkingOrbis.sendPacketToDimension(new PacketWorldObjectAdd(world, group, region), world.provider.getDimension());
			}

			if (this.power.getSelectedRegion() != null)
			{
				NetworkingOrbis.sendPacketToDimension(new PacketWorldObjectRemove(world, group, this.power.getSelectedRegion()), world.provider.getDimension());
				group.removeObject(this.power.getSelectedRegion());
			}

			this.power.setSelectedRegion(region);

			NetworkingOrbis.sendPacketToPlayer(new PacketSetSelectedRegion(regionId), (EntityPlayerMP) playerOrbis.getEntity());
		}
	}
}
