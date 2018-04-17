package com.gildedgames.orbis.api;

import com.gildedgames.orbis.api.packets.instances.INetworkOrbis;
import com.gildedgames.orbis.api.world.WorldObjectManagerEvents;
import com.gildedgames.orbis.api.world.instances.IInstanceRegistry;
import com.gildedgames.orbis.api.world.instances.InstanceEvents;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This OrbisAPI allows mod developers to integrate
 * the projects they've developed with the Orbis tool
 * into their mod designs. It's recommended you create an
 * IOrbisDefinitionRegistry and register it in this API's services.
 */
public class OrbisAPI
{
	public static final Logger LOGGER = LogManager.getLogger("OrbisAPI");

	private static IOrbisServices services;

	private OrbisAPI()
	{

	}

	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getSide().isClient();
	}

	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getSide().isServer();
	}

	public static IOrbisServices services()
	{
		if (OrbisAPI.services == null)
		{
			OrbisAPI.services = new OrbisServices();

			MinecraftForge.EVENT_BUS.register(InstanceEvents.class);
			MinecraftForge.EVENT_BUS.register(WorldObjectManagerEvents.class);
		}

		return OrbisAPI.services;
	}

	public static IInstanceRegistry instances()
	{
		return OrbisAPI.services().instances();
	}

	public static INetworkOrbis network()
	{
		return OrbisAPI.services().network();
	}

	public static File getWorldDirectory()
	{
		return DimensionManager.getCurrentSaveRootDirectory();
	}
}
