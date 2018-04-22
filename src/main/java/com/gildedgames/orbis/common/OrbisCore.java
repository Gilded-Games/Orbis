package com.gildedgames.orbis.common;

import com.gildedgames.orbis_api.IOrbisServicesListener;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.management.IDataCachePool;
import com.gildedgames.orbis_api.data.management.IProjectManager;
import com.gildedgames.orbis_api.data.management.impl.DataCache;
import com.gildedgames.orbis_api.data.management.impl.DataCachePool;
import com.gildedgames.orbis_api.network.INetworkMultipleParts;
import com.gildedgames.orbis_api.util.io.IClassSerializer;
import com.gildedgames.orbis_api.util.io.Instantiator;
import com.gildedgames.orbis_api.util.io.SimpleSerializer;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.gildedgames.orbis_api.world.instances.InstanceEvents;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.capabilities.CapabilityManagerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintNode;
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
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstance;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.common.world_actions.impl.*;
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

@Mod(name = OrbisCore.MOD_NAME, modid = OrbisCore.MOD_ID, version = OrbisCore.MOD_VERSION,
		dependencies = OrbisCore.MOD_DEPENDENCIES)
@Mod.EventBusSubscriber
public class OrbisCore implements IOrbisServicesListener
{

	public static final String MOD_NAME = "Orbis";

	public static final String MOD_ID = "orbis";

	public static final String MOD_VERSION = "1.12.2-1.0.0";

	public static final String MOD_DEPENDENCIES = "required-after:orbis_api";

	public static final Logger LOGGER = LogManager.getLogger("Orbis");

	public static final String BLOCK_DATA_CONTAINERS_CACHE = "block_data_containers";

	@Mod.Instance(OrbisCore.MOD_ID)
	public static OrbisCore INSTANCE;

	@SidedProxy(clientSide = "com.gildedgames.orbis.client.ClientProxy", serverSide = "com.gildedgames.orbis.common.CommonProxy")
	public static CommonProxy PROXY;

	public static ConfigOrbis CONFIG;

	public static OrbisInstanceHandler ORBIS_INSTANCE_HANDLER;

	private static IDataCachePool dataCache;

	private static boolean loadedInstances;

	private static void clearSelection(final EntityPlayer player)
	{
		final World world = player.world;
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis.powers().getSelectPower().getSelectedRegion() != null && !world.isRemote)
		{
			OrbisCore.network().sendPacketToServer(new PacketClearSelectedRegion());
			OrbisCore.network().sendPacketToServer(new PacketWorldObjectRemove(world, playerOrbis.powers().getSelectPower().getSelectedRegion()));

			playerOrbis.powers().getSelectPower().setSelectedRegion(null);
		}
	}

	public static IProjectManager getProjectManager()
	{
		return OrbisAPI.services().getProjectManager();
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
		if (!event.player.world.isRemote)
		{
			OrbisCore.network().sendPacketToPlayer(new PacketSendProjectListing(), (EntityPlayerMP) event.player);
			OrbisCore.network().sendPacketToPlayer(new PacketSendDataCachePool(getDataCache()), (EntityPlayerMP) event.player);
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

				OrbisCore.network().sendPacketToPlayer(new PacketWorldObjectManager(manager), (EntityPlayerMP) player);
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

	public static INetworkMultipleParts network()
	{
		return NetworkingOrbis.network();
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

	public static void saveDataCache()
	{
		if (dataCache != null)
		{
			dataCache.flushToDisk();
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
		s.register(8, BlueprintDataPalette.class, new Instantiator<>(BlueprintDataPalette.class));
		s.register(9, ColoredRegion.class, new Instantiator<>(ColoredRegion.class));
		s.register(10, Framework.class, new Instantiator<>(Framework.class));
		s.register(11, BlueprintNode.class, new Instantiator<>(BlueprintNode.class));
		s.register(12, OrbisInstance.class, new Instantiator<>(OrbisInstance.class));

		s.register(13, WorldActionPathway.class, new Instantiator<>(WorldActionPathway.class));
		s.register(14, WorldActionAddWorldObject.class, new Instantiator<>(WorldActionAddWorldObject.class));
		s.register(15, WorldActionAddEntrance.class, new Instantiator<>(WorldActionAddEntrance.class));
		s.register(16, WorldActionAddSchedule.class, new Instantiator<>(WorldActionAddSchedule.class));
		s.register(17, WorldActionFilter.class, new Instantiator<>(WorldActionFilter.class));
		s.register(18, WorldActionBlueprint.class, new Instantiator<>(WorldActionBlueprint.class));
		s.register(19, WorldActionBlockDataContainer.class, new Instantiator<>(WorldActionBlockDataContainer.class));
		s.register(20, WorldActionBlueprintPalette.class, new Instantiator<>(WorldActionBlueprintPalette.class));
		s.register(21, WorldActionAddBlueprint.class, new Instantiator<>(WorldActionAddBlueprint.class));
		s.register(22, WorldActionBlueprintStacker.class, new Instantiator<>(WorldActionBlueprintStacker.class));

		OrbisAPI.services().io().register(s);
	}

	@Mod.EventHandler
	public void onFMLConstruction(final FMLConstructionEvent event)
	{
		registerSerializations();
	}

	@Mod.EventHandler
	public void onFMLPreInit(final FMLPreInitializationEvent event)
	{
		NetworkingOrbis.preInit();

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
		OrbisAPI.services().stopProjectManager();
		stopDataCache();

		//TODO: Move this over to WorldData saver or something. This currently sucks.
		InstanceEvents.saveAllInstancesToDisk();

		loadedInstances = false;
	}

	@Mod.EventHandler
	public void onServerStopped(final FMLServerStoppedEvent event)
	{
		InstanceEvents.unregisterAllInstances();
	}

	@Mod.EventHandler
	public void serverStarted(final FMLServerStartedEvent event)
	{
		// Checks if listener is already in, don't worry
		OrbisAPI.services().listen(OrbisCore.INSTANCE);
		OrbisAPI.services().startProjectManager();
		startDataCache();

		if (!loadedInstances)
		{
			InstanceEvents.loadAllInstancesFromDisk();

			loadedInstances = true;
		}
	}

	@Override
	public void onStartProjectManager()
	{
		OrbisAPI.services().getProjectManager().scanAndCacheProjects();
	}
}