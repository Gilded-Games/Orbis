package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.packets.PacketWorldSeed;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;

public class WorldObjectManagerEvents
{

	@SubscribeEvent
	public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
	{
		for (Map.Entry<Integer, Long> entry : WorldObjectManager.getWorldSeeds().entrySet())
		{
			int dimension = entry.getKey();
			long seed = entry.getValue();

			OrbisAPI.network().sendPacketToAllPlayers(new PacketWorldSeed(dimension, seed));
		}
	}

	@SubscribeEvent
	public static void onWorldTick(final TickEvent.WorldTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			final World world = event.world;

			if (!world.isRemote)
			{
				if (!WorldObjectManager.hasWorldSeed(world.provider.getDimension()))
				{
					WorldObjectManager.setWorldSeed(world.provider.getDimension(), world.getSeed());

					OrbisAPI.network().sendPacketToAllPlayers(new PacketWorldSeed(world.provider.getDimension(), world.getSeed()));
				}
			}
		}
	}

}
