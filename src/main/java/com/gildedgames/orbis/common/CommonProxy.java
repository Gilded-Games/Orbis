package com.gildedgames.orbis.common;

import com.gildedgames.orbis.common.dungeons.DungeonViewer;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceFactory;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import com.gildedgames.orbis.lib.OrbisLib;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{

	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new DungeonViewer());
		OrbisLib.services().setProjectManagerInitSource(OrbisCore.INSTANCE, "orbis");

		WorldProviderOrbis.preInit();

		final OrbisInstanceFactory factory = new OrbisInstanceFactory(WorldProviderOrbis.ORBIS);

		OrbisCore.ORBIS_INSTANCE_HANDLER = new OrbisInstanceHandler(OrbisLib.instances().createInstanceHandler(factory));
	}

	public void init(FMLInitializationEvent event)
	{

	}

	public EntityPlayer getPlayer()
	{
		return null;
	}

}
