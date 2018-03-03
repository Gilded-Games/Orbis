package com.gildedgames.orbis.api;

import com.gildedgames.orbis.api.world.instances.IPlayerInstances;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class OrbisAPICapabilities
{
	@CapabilityInject(IPlayerInstances.class)
	public static final Capability<IPlayerInstances> PLAYER_INSTANCES = null;
}
