package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.player.godmode.GodPowerFramework;
import com.gildedgames.orbis.common.world_objects.Framework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShapeSelectorFramework implements IShapeSelector
{
	private final GodPowerFramework power;

	public ShapeSelectorFramework(final GodPowerFramework power)
	{
		this.power = power;
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return held.isEmpty();
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		return true;
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		final Framework framework = new Framework(world, selectedShape.getBoundingBox());

		if (!world.isRemote)
		{
			group.addObject(framework);

			if (world.getMinecraftServer().isDedicatedServer())
			{
				NetworkingOrbis.sendPacketToDimension(new PacketWorldObjectAdd(world, group, framework), world.provider.getDimension());
			}
		}
	}
}
