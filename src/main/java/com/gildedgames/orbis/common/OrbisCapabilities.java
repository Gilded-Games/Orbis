package com.gildedgames.orbis.common;

import com.gildedgames.orbis.common.capabilities.chunk_renderer.IChunkRendererCapability;
import com.gildedgames.orbis.player.IPlayerOrbis;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class OrbisCapabilities
{
	@CapabilityInject(IPlayerOrbis.class)
	public static final Capability<IPlayerOrbis> PLAYER_DATA = null;

	@CapabilityInject(IChunkRendererCapability.class)
	public static final Capability<IChunkRendererCapability> CHUNK_RENDERER = null;
}
