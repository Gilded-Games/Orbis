package com.gildedgames.orbis.common.capabilities;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.OrbisAPICapabilities;
import com.gildedgames.orbis.api.world.instances.IPlayerInstances;
import com.gildedgames.orbis.api.world.instances.PlayerInstances;
import com.gildedgames.orbis.api.world.instances.PlayerInstancesProvider;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.ChunkRenderer;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.ChunkRendererProvider;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.IChunkRendererCapability;
import com.gildedgames.orbis.common.capabilities.player.IPlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber()
public class CapabilityManagerOrbis
{
	public static void init()
	{
		CapabilityManager.INSTANCE.register(IChunkRendererCapability.class, new ChunkRenderer.Storage(), ChunkRenderer::new);
		CapabilityManager.INSTANCE.register(IPlayerOrbis.class, new PlayerOrbis.Storage(), PlayerOrbis::new);
		CapabilityManager.INSTANCE.register(IPlayerInstances.class, new PlayerInstances.Storage(), PlayerInstances::new);
	}

	@SubscribeEvent
	public static void attachChunk(final AttachCapabilitiesEvent<Chunk> event)
	{
		if (event.getObject() == null)
		{
			return;
		}

		if (event.getObject() instanceof Chunk)
		{
			Chunk c = event.getObject();

			event.addCapability(OrbisCore.getResource("ChunkRenderer"), new ChunkRendererProvider(new ChunkRenderer(c.x, c.z)));
		}
	}

	@SubscribeEvent
	public static void onEntityLoad(final AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() == null)
		{
			return;
		}

		if (event.getObject() instanceof EntityPlayer)
		{
			event.addCapability(OrbisCore.getResource("PlayerData"), new PlayerOrbisProvider(new PlayerOrbis((EntityPlayer) event.getObject())));
			event.addCapability(OrbisCore.getResource("PlayerInstances"), new PlayerInstancesProvider((EntityPlayer) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(final PlayerEvent.Clone event)
	{
		final IPlayerInstances oldPlayer = OrbisAPI.instances().getPlayer(event.getOriginal());

		if (oldPlayer != null)
		{
			final IPlayerInstances newPlayer = OrbisAPI.instances().getPlayer((EntityPlayer) event.getEntity());

			final Capability.IStorage<IPlayerInstances> storage = OrbisAPICapabilities.PLAYER_INSTANCES.getStorage();

			final NBTBase state = storage.writeNBT(OrbisAPICapabilities.PLAYER_INSTANCES, oldPlayer, null);

			storage.readNBT(OrbisAPICapabilities.PLAYER_INSTANCES, newPlayer, null, state);
		}
	}

}
