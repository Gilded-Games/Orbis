package com.gildedgames.orbis.common.player.godmode.selectors;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
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
		return !WorldObjectUtils.isIntersectingShapes(world, shape);
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final Framework framework = new Framework(world, selectedShape.getBoundingBox());

		if (!world.isRemote)
		{
			if (world.getMinecraftServer().isDedicatedServer())
			{
				manager.addObject(framework);
			}

			OrbisCore.network().sendPacketToDimension(new PacketWorldObjectAdd(framework), world.provider.getDimension());
		}
	}
}
