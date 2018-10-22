package com.gildedgames.orbis.common;

import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.capabilities.CapabilityManagerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintNode;
import com.gildedgames.orbis.common.items.ItemBlockPalette;
import com.gildedgames.orbis.common.network.CommandActivateDesignerGamemode;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSendDataCachePool;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectManager;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.network.packets.projects.PacketSendProjectListing;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeCuboid;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeLine;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeSphere;
import com.gildedgames.orbis.common.tiles.OrbisTileEntities;
import com.gildedgames.orbis.common.util.ColoredRegion;
import com.gildedgames.orbis.common.variables.GuiVarProjectFile;
import com.gildedgames.orbis.common.variables.post_resolve_actions.PostResolveActionApplyLootTable;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstance;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.common.world_actions.impl.*;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis.common.world_objects.WorldRegion;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis_api.IOHelper;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.block.BlockDataWithConditions;
import com.gildedgames.orbis_api.block.BlockFilterHelper;
import com.gildedgames.orbis_api.block.BlockFilterLayer;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.management.IDataCachePool;
import com.gildedgames.orbis_api.data.management.IProjectManager;
import com.gildedgames.orbis_api.data.management.impl.DataCache;
import com.gildedgames.orbis_api.data.management.impl.DataCachePool;
import com.gildedgames.orbis_api.network.INetworkMultipleParts;
import com.gildedgames.orbis_api.util.io.IClassSerializer;
import com.gildedgames.orbis_api.util.io.SimpleSerializer;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import java.util.Collections;
import java.util.List;

@Mod(name = OrbisCore.MOD_NAME, modid = OrbisCore.MOD_ID, version = OrbisCore.MOD_VERSION,
		dependencies = OrbisCore.MOD_DEPENDENCIES, certificateFingerprint = OrbisCore.MOD_FINGERPRINT)
@Mod.EventBusSubscriber
public class OrbisCore
{

	public static final String MOD_FINGERPRINT = "db341c083b1b8ce9160a769b569ef6737b3f4cdf";

	public static final String MOD_NAME = "Orbis";

	public static final String MOD_ID = "orbis";

	public static final String MOD_VERSION = "1.12.2-1.0.13";

	public static final String MOD_DEPENDENCIES = "required-after:orbis_api@[1.12.2-1.1.11,)";

	public static final Logger LOGGER = LogManager.getLogger("Orbis");

	public static final String BLOCK_DATA_CONTAINERS_CACHE = "block_data_containers";

	@Mod.Instance(OrbisCore.MOD_ID)
	public static OrbisCore INSTANCE;

	@SidedProxy(clientSide = "com.gildedgames.orbis.client.ClientProxy", serverSide = "com.gildedgames.orbis.common.CommonProxy")
	public static CommonProxy PROXY;

	public static ConfigOrbis CONFIG;

	public static OrbisInstanceHandler ORBIS_INSTANCE_HANDLER;

	private static IDataCachePool dataCache;

	private static void clearSelection(final EntityPlayer player)
	{
		final World world = player.world;
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis.powers().getSelectPower().getSelectedRegion() != null && !world.isRemote)
		{
			final WorldObjectManager manager = WorldObjectManager.get(player.world);

			WorldShape selection = playerOrbis.powers().getSelectPower().getSelectedRegion();
			int id = manager.getID(selection);

			manager.removeObject(id);

			OrbisCore.network()
					.sendPacketToDimension(new PacketWorldObjectRemove(id, world.provider.getDimension()), world.provider.getDimension());

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
			if (OrbisCore.CONFIG.useExperimentalFeatures())
			{
				OrbisCore.network().sendPacketToPlayer(new PacketSendProjectListing(), (EntityPlayerMP) event.player);
				OrbisCore.network().sendPacketToPlayer(new PacketSendDataCachePool(getDataCache()), (EntityPlayerMP) event.player);
			}
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

				if (OrbisCore.CONFIG.useExperimentalFeatures())
				{
					OrbisCore.network().sendPacketToPlayer(new PacketWorldObjectManager(manager), (EntityPlayerMP) player);
				}
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

		IOHelper.register(s, 0, RenderShape.class);
		IOHelper.register(s, 1, WorldRegion.class);
		IOHelper.register(s, 2, Blueprint.class);
		IOHelper.register(s, 3, SelectionTypeCuboid.class);
		IOHelper.register(s, 4, SelectionTypeSphere.class);
		IOHelper.register(s, 5, SelectionTypeLine.class);
		IOHelper.register(s, 6, Text.class);
		IOHelper.register(s, 7, WorldShape.class);
		IOHelper.register(s, 8, BlueprintDataPalette.class);
		IOHelper.register(s, 9, ColoredRegion.class);
		IOHelper.register(s, 10, Framework.class);
		IOHelper.register(s, 11, BlueprintNode.class);
		IOHelper.register(s, 12, OrbisInstance.class);

		IOHelper.register(s, 13, WorldActionPathway.class);
		IOHelper.register(s, 14, WorldActionAddWorldObject.class);
		IOHelper.register(s, 15, WorldActionAddEntrance.class);
		IOHelper.register(s, 16, WorldActionAddSchedule.class);
		IOHelper.register(s, 17, WorldActionFilter.class);
		IOHelper.register(s, 18, WorldActionBlueprint.class);
		IOHelper.register(s, 19, WorldActionBlockDataContainer.class);
		IOHelper.register(s, 20, WorldActionBlueprintPalette.class);
		IOHelper.register(s, 21, WorldActionAddBlueprint.class);
		IOHelper.register(s, 22, WorldActionBlueprintStacker.class);
		IOHelper.register(s, 23, WorldActionFilterMultiple.class);

		IOHelper.register(s, 24, PostResolveActionApplyLootTable.class);
		IOHelper.register(s, 25, GuiVarProjectFile.class);

		OrbisAPI.services().io().register(s);
	}

	@Mod.EventHandler
	public void onFMLConstruction(final FMLConstructionEvent event)
	{
		// Registration so BlockFilterHelper recognizes ItemBlockPalette when trying to fetch blocks
		BlockFilterHelper.registerBlockRecognition(new BlockFilterHelper.IBlockRecognition()
		{
			@Override
			public List<BlockDataWithConditions> recognize(ItemStack stack)
			{
				List<BlockDataWithConditions> blocks = Collections.emptyList();

				if (stack.getItem() instanceof ItemBlockPalette)
				{
					BlockFilterLayer layer = ItemBlockPalette.getFilterLayer(stack);

					if (layer != null)
					{
						blocks = Lists.newArrayList();

						blocks.addAll(layer.getReplacementBlocks());
					}
				}

				return blocks;
			}

			@Override
			public boolean isCompatible(Class<? extends Item> clazz)
			{
				return ItemBlockPalette.class.isAssignableFrom(clazz);
			}
		});

		registerSerializations();
	}

	@Mod.EventHandler
	public void onFMLPreInit(final FMLPreInitializationEvent event)
	{
		OrbisCore.CONFIG = new ConfigOrbis(event.getSuggestedConfigurationFile());

		if (OrbisCore.CONFIG.useExperimentalFeatures())
		{
			OrbisAPI.services().enableScanAndCacheProjectsOnStartup(true);
		}

		NetworkingOrbis.preInit();

		OrbisTileEntities.preInit();

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
		if (OrbisCore.CONFIG.useExperimentalFeatures())
		{
			OrbisAPI.services().stopProjectManager();
		}

		stopDataCache();
	}

	@Mod.EventHandler
	public void onServerStopped(final FMLServerStoppedEvent event)
	{

	}

	@Mod.EventHandler
	public void serverStarted(final FMLServerStartedEvent event)
	{
		if (OrbisCore.CONFIG.useExperimentalFeatures())
		{
			OrbisAPI.services().startProjectManager();
		}

		startDataCache();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandActivateDesignerGamemode());
	}
}