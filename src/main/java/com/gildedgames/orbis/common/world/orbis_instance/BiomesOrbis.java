package com.gildedgames.orbis.common.world.orbis_instance;

import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber()
@GameRegistry.ObjectHolder(OrbisCore.MOD_ID)
public class BiomesOrbis
{
	@GameRegistry.ObjectHolder("instanced_zone")
	public static final Biome INSTANCED_ZONE = new BiomeInstancedZone();

	@SubscribeEvent
	public static void registerBiome(final RegistryEvent.Register<Biome> event)
	{
		event.getRegistry().registerAll(INSTANCED_ZONE);
	}
}
