package com.gildedgames.orbis.api;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This OrbisAPI allows mod developers to integrate
 * the projects they've developed with the Orbis tool
 * into their mod designs. It's recommended you create an
 * IOrbisDefinitionRegistry and register it in this API's services.
 */
public class OrbisAPI
{
	private static IOrbisServices services;

	public static final Logger LOGGER = LogManager.getLogger("OrbisAPI");

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
		}

		return OrbisAPI.services;
	}
}
