package com.gildedgames.orbis.common.capabilities.player;

import com.gildedgames.orbis.common.OrbisCapabilities;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

@Mod.EventBusSubscriber()
public class PlayerOrbisEvents
{
	@SubscribeEvent
	public static void onPlayerJoined(final PlayerLoggedInEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(event.player);

		if (playerOrbis != null)
		{
			playerOrbis.sendFullUpdate();
		}
	}

	@SubscribeEvent
	public static void onUpdate(final LivingUpdateEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(event.getEntity());

		if (playerOrbis != null)
		{
			playerOrbis.onUpdate(event);
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(final PlayerEvent.Clone event)
	{
		final PlayerOrbis oldPlayer = PlayerOrbis.get(event.getOriginal());

		if (oldPlayer != null)
		{
			final PlayerOrbis newPlayer = PlayerOrbis.get(event.getEntity());

			final IStorage<IPlayerOrbis> storage = OrbisCapabilities.PLAYER_DATA.getStorage();

			final NBTBase state = storage.writeNBT(OrbisCapabilities.PLAYER_DATA, oldPlayer, null);
			storage.readNBT(OrbisCapabilities.PLAYER_DATA, newPlayer, null, state);
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(final PlayerChangedDimensionEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(event.player);

		if (playerOrbis != null)
		{
			playerOrbis.onTeleport(event);
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(final PlayerRespawnEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(event.player);

		if (playerOrbis != null)
		{
			playerOrbis.onRespawn(event);
		}
	}

	@SubscribeEvent
	public static void onBeginWatching(final PlayerEvent.StartTracking event)
	{
		final PlayerOrbis aeSourcePlayer = PlayerOrbis.get(event.getEntityPlayer());
		final PlayerOrbis aeTargetPlayer = PlayerOrbis.get(event.getTarget());

		if (aeSourcePlayer != null && aeTargetPlayer != null)
		{
			aeTargetPlayer.onPlayerBeginWatching(aeSourcePlayer);
		}
	}
}
