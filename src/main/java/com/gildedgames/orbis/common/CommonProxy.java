package com.gildedgames.orbis.common;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceFactory;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{

	public void preInit(FMLPreInitializationEvent event)
	{
		WorldProviderOrbis.preInit();

		final OrbisInstanceFactory factory = new OrbisInstanceFactory(WorldProviderOrbis.ORBIS);

		OrbisCore.ORBIS_INSTANCE_HANDLER = new OrbisInstanceHandler(OrbisAPI.instances().createAndRegisterInstanceHandler(factory));
	}

	public void init(FMLInitializationEvent event)
	{

	}

}
