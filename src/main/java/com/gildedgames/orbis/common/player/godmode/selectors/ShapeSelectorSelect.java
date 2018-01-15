package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetSelectedRegion;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
		return true;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world)
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

			final ICreationData creationData = new CreationData(world, playerOrbis.getEntity());
			filter.apply(selectedShape, world, creationData);
			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

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
