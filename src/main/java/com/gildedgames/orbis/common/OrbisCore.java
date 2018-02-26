package com.gildedgames.orbis.common;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.management.IDataCachePool;
import com.gildedgames.orbis.api.data.management.IProjectManager;
import com.gildedgames.orbis.api.data.management.impl.DataCache;
import com.gildedgames.orbis.api.data.management.impl.DataCachePool;
import com.gildedgames.orbis.api.data.management.impl.OrbisProjectManager;
import com.gildedgames.orbis.api.util.io.IClassSerializer;
import com.gildedgames.orbis.api.util.io.Instantiator;
import com.gildedgames.orbis.api.util.io.SimpleSerializer;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.capabilities.CapabilityManagerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintNode;
import com.gildedgames.orbis.common.data.BlueprintPalette;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketClearSelectedRegion;
import com.gildedgames.orbis.common.network.packets.PacketSendDataCachePool;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectManager;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.network.packets.projects.PacketSendProjectListing;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeCuboid;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeLine;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeSphere;
import com.gildedgames.orbis.common.tiles.OrbisTileEntities;
import com.gildedgames.orbis.common.util.ColoredRegion;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis.common.world_objects.WorldRegion;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(name = OrbisCore.MOD_NAME, modid = OrbisCore.MOD_ID, version = OrbisCore.MOD_VERSION)
public class OrbisCore
{

	public static final String MOD_NAME = "Orbis";

	public static final String MOD_ID = "orbis";

	public static final String MOD_VERSION = "1.12.2-1.0.0";

	public static final Logger LOGGER = LogManager.getLogger("Orbis");

	public static final String BLOCK_DATA_CONTAINERS_CACHE = "block_data_containers";

	@Mod.Instance(OrbisCore.MOD_ID)
	public static OrbisCore INSTANCE;

	@SidedProxy(clientSide = "com.gildedgames.orbis.client.ClientProxy", serverSide = "com.gildedgames.orbis.common.CommonProxy")
	public static CommonProxy PROXY;

	public static ConfigOrbis CONFIG;

	private static IProjectManager projectManager;

	private static IDataCachePool dataCache;

	private static void clearSelection(final EntityPlayer player)
	{
		final World world = player.world;
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis.powers().getSelectPower().getSelectedRegion() != null && !world.isRemote)
		{
			final WorldObjectManager manager = WorldObjectManager.get(world);
			final IWorldObjectGroup group = manager.getGroup(0);

			NetworkingOrbis.sendPacketToServer(new PacketClearSelectedRegion());
			NetworkingOrbis.sendPacketToServer(new PacketWorldObjectRemove(world, group, playerOrbis.powers().getSelectPower().getSelectedRegion()));

			playerOrbis.powers().getSelectPower().setSelectedRegion(null);
		}
	}

	@SubscribeEvent
	public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
	{
		/**
		 * Will only send if the server is a dedicated server.
		 * This ensures that on singleplayer, the client uses the same
		 * directory/state as the integrated server (instead of having to
		 * "download" state from the integrated server).
		 */
		if (!event.player.world.isRemote && event.player.getServer() != null && event.player.getServer().isDedicatedServer())
		{
			NetworkingOrbis.sendPacketToPlayer(new PacketSendProjectListing(), (EntityPlayerMP) event.player);
			NetworkingOrbis.sendPacketToPlayer(new PacketSendDataCachePool(dataCache), (EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(final EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			final EntityPlayer player = (EntityPlayer) event.getEntity();
			final World world = player.world;

			if (!world.isRemote)
			{
				final WorldObjectManager manager = WorldObjectManager.get(player.getServer().getWorld(world.provider.getDimension()));

				NetworkingOrbis.sendPacketToPlayer(new PacketWorldObjectManager(manager), (EntityPlayerMP) player);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event)
	{
		clearSelection(event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event)
	{
		clearSelection(event.player);
	}

	public synchronized static void startProjectManager()
	{
		if (projectManager != null)
		{
			return;
		}

		if (isClient())
		{
			final ServerData data = Minecraft.getMinecraft().getCurrentServerData();

			if (data != null)
			{
				projectManager = new OrbisProjectManager(
						new File(Minecraft.getMinecraft().mcDataDir, "/orbis/servers/" + data.serverIP.replace(":", "_") + "/projects/"));
			}
			else
			{
				projectManager = new OrbisProjectManager(new File(Minecraft.getMinecraft().mcDataDir, "/orbis/local/projects/"));
			}
		}

		if (projectManager == null)
		{
			projectManager = new OrbisProjectManager(new File(DimensionManager.getCurrentSaveRootDirectory(), "/orbis/projects/"));
		}

		projectManager.scanAndCacheProjects(OrbisCore.INSTANCE, "Orbis");
	}

	public synchronized static void stopProjectManager()
	{
		if (projectManager != null)
		{
			projectManager.flushProjects();
			projectManager = null;
		}
	}

	public synchronized static IProjectManager getProjectManager()
	{
		if (projectManager == null)
		{
			startProjectManager();
		}

		return projectManager;
	}

	public static void startDataCache()
	{
		if (dataCache != null)
		{
			return;
		}

		if (isClient())
		{
			final ServerData data = Minecraft.getMinecraft().getCurrentServerData();

			if (data != null)
			{
				dataCache = new DataCachePool(
						new File(Minecraft.getMinecraft().mcDataDir, "/orbis/servers/" + data.serverIP.replace(":", "_") + "/cache/"));
			}
			else
			{
				dataCache = new DataCachePool(new File(Minecraft.getMinecraft().mcDataDir, "/orbis/local/cache/"));
			}
		}

		if (dataCache == null)
		{
			dataCache = new DataCachePool(new File(DimensionManager.getCurrentSaveRootDirectory(), "/orbis/cache/"));
		}

		dataCache.readFromDisk();

		if (dataCache.findCache(OrbisCore.BLOCK_DATA_CONTAINERS_CACHE) == null)
		{
			dataCache.registerCache(new DataCache(OrbisCore.BLOCK_DATA_CONTAINERS_CACHE));
		}
	}

	public static void stopDataCache()
	{
		if (dataCache != null)
		{
			dataCache.flushToDisk();
			dataCache = null;
		}
	}

	public static IDataCachePool getDataCache()
	{
		if (dataCache == null)
		{
			startDataCache();
		}

		return dataCache;
	}

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

	public static void registerSerializations()
	{
		final IClassSerializer s = new SimpleSerializer("orbis");

		s.register(0, RenderShape.class, new Instantiator<>(RenderShape.class));
		s.register(1, WorldRegion.class, new Instantiator<>(WorldRegion.class));
		s.register(2, Blueprint.class, new Instantiator<>(Blueprint.class));
		s.register(3, SelectionTypeCuboid.class, new Instantiator<>(SelectionTypeCuboid.class));
		s.register(4, SelectionTypeSphere.class, new Instantiator<>(SelectionTypeSphere.class));
		s.register(5, SelectionTypeLine.class, new Instantiator<>(SelectionTypeLine.class));
		s.register(6, Text.class, new Instantiator<>(Text.class));
		s.register(7, WorldShape.class, new Instantiator<>(WorldShape.class));
		s.register(8, BlueprintPalette.class, new Instantiator<>(BlueprintPalette.class));
		s.register(9, ColoredRegion.class, new Instantiator<>(ColoredRegion.class));
		s.register(10, Framework.class, new Instantiator<>(Framework.class));
		s.register(11, BlueprintNode.class, new Instantiator<>(BlueprintNode.class));

		OrbisAPI.services().io().register(s);
	}

	@Mod.EventHandler
	public void onFMLConstruction(final FMLConstructionEvent event)
	{

	}

	@Mod.EventHandler
	public void onFMLPreInit(final FMLPreInitializationEvent event)
	{
		NetworkingOrbis.preInit();

		registerSerializations();

		OrbisTileEntities.preInit();

		OrbisCore.CONFIG = new ConfigOrbis(event.getSuggestedConfigurationFile());
		OrbisCore.PROXY.preInit(event);
	}

	@Mod.EventHandler
	public void onFMLInit(final FMLInitializationEvent event)
	{
		CapabilityManagerOrbis.init();

		OrbisCore.PROXY.init(event);
	}

	@Mod.EventHandler
	public void onServerStopping(final FMLServerStoppingEvent event)
	{
		stopProjectManager();
		startDataCache();
	}

	@Mod.EventHandler
	public void serverStarted(final FMLServerStartedEvent event)
	{
		startProjectManager();
		stopDataCache();
	}

}