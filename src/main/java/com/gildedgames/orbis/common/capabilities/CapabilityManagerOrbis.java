package com.gildedgames.orbis.common.capabilities;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.ChunkRenderer;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.ChunkRendererProvider;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.IChunkRendererCapability;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisProvider;
import com.gildedgames.orbis.player.IPlayerOrbis;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber()
public class CapabilityManagerOrbis
{
	public static void init()
	{
		CapabilityManager.INSTANCE.register(IChunkRendererCapability.class, new ChunkRenderer.Storage(), ChunkRenderer::new);
		CapabilityManager.INSTANCE.register(IPlayerOrbis.class, new PlayerOrbis.Storage(), PlayerOrbis::new);
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
		}
	}

}
