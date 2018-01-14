package com.gildedgames.orbis.common;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(name = OrbisCore.MOD_NAME, modid = OrbisCore.MOD_ID, version = OrbisCore.MOD_VERSION)
public class OrbisCore
{

	public static final String MOD_NAME = "Orbis";

	public static final String MOD_ID = "orbis";

	public static final String MOD_VERSION = "1.11.2-1.0.0";

	public static final Logger LOGGER = LogManager.getLogger("Orbis");

	@Mod.Instance(OrbisCore.MOD_ID)
	public static OrbisCore INSTANCE;

	@SidedProxy(clientSide = "com.gildedgames.orbis.client.ClientProxy", serverSide = "com.gildedgames.orbis.common.CommonProxy")
	public static CommonProxy PROXY;

	public static ConfigOrbis CONFIG;

	public static ResourceLocation getResource(final String name)
	{
		return new ResourceLocation(OrbisCore.MOD_ID, name);
	}

	public static String getResourcePath(final String name)
	{
		return (OrbisCore.MOD_ID + ":") + name;
	}

	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getSide().isClient();
	}

	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getSide().isServer();
	}

	public static File getWorldDirectory()
	{
		return DimensionManager.getCurrentSaveRootDirectory();
	}

	public static boolean isInsideDevEnvironment()
	{
		return Launch.blackboard.get("fml.deobfuscatedEnvironment") == Boolean.TRUE;
	}

	@Mod.EventHandler
	public void onFMLConstruction(final FMLConstructionEvent event)
	{

	}

	@Mod.EventHandler
	public void onFMLPreInit(final FMLPreInitializationEvent event)
	{
		OrbisCore.CONFIG = new ConfigOrbis(event.getSuggestedConfigurationFile());
		OrbisCore.PROXY.preInit(event);
	}

	@Mod.EventHandler
	public void onFMLInit(final FMLInitializationEvent event)
	{
		OrbisCore.PROXY.init(event);
	}

	@Mod.EventHandler
	public void onServerStopping(final FMLServerStoppingEvent event)
	{

	}

	@Mod.EventHandler
	public void serverStarted(final FMLServerStartedEvent event)
	{

	}

}