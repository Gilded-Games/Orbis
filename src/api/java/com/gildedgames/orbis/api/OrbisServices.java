package com.gildedgames.orbis.api;

import com.gildedgames.orbis.api.block.*;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.core.GameRegistrar;
import com.gildedgames.orbis.api.core.PlacedBlueprint;
import com.gildedgames.orbis.api.core.registry.IOrbisDefinitionRegistry;
import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.DataCondition;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import com.gildedgames.orbis.api.data.management.IProject;
import com.gildedgames.orbis.api.data.management.IProjectManager;
import com.gildedgames.orbis.api.data.management.impl.*;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.data.shapes.*;
import com.gildedgames.orbis.api.inventory.InventorySpawnEggs;
import com.gildedgames.orbis.api.packets.instances.INetworkOrbis;
import com.gildedgames.orbis.api.util.io.IClassSerializer;
import com.gildedgames.orbis.api.util.io.Instantiator;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.io.SimpleSerializer;
import com.gildedgames.orbis.api.util.mc.BlockPosDimension;
import com.gildedgames.orbis.api.world.instances.IInstanceRegistry;
import com.gildedgames.orbis.api.world.instances.InstanceRegistryImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class OrbisServices implements IOrbisServices
{
	public final Logger logger = LogManager.getLogger("OrbisAPI");

	private final Map<String, IOrbisDefinitionRegistry> idToRegistry = Maps.newHashMap();

	private final String baseFolder = "orbis";

	private final Map<String, IProject> loadedProjects = Maps.newHashMap();

	private final GameRegistrar gameRegistrar = new GameRegistrar();

	private List<IOrbisServicesListener> listeners = Lists.newArrayList();

	private IProjectManager projectManager;

	private IOHelper io;

	private INetworkOrbis network;

	private IInstanceRegistry instancesRegistry = new InstanceRegistryImpl();

	private Object mod;

	private String archiveBaseName;

	@Override
	public Logger log()
	{
		return this.logger;
	}

	@Nullable
	@Override
	public IOrbisDefinitionRegistry findDefinitionRegistry(final String registryId)
	{
		final IOrbisDefinitionRegistry registry = this.idToRegistry.get(registryId);

		return registry;
	}

	@Override
	public void register(final IOrbisDefinitionRegistry registry)
	{
		if (registry == null)
		{
			throw new RuntimeException("Registry provided is null! Aborting.");
		}

		this.idToRegistry.put(registry.getRegistryId(), registry);
	}

	@Override
	@Nullable
	public IProject loadProject(final MinecraftServer server, final ResourceLocation location, Object mod, String archiveBaseName)
	{
		return this.get(server, location, mod, archiveBaseName);
	}

	@Override
	public IOHelper io()
	{
		if (this.io == null)
		{
			this.io = new IOHelper();

			final IClassSerializer s = new SimpleSerializer("orbis_api");

			s.register(0, Region.class, new Instantiator<>(Region.class));
			s.register(1, BlueprintData.class, new Instantiator<>(BlueprintData.class));
			s.register(3, BlockDataContainer.class, new Instantiator<>(BlockDataContainer.class));
			s.register(4, SphereShape.class, new Instantiator<>(SphereShape.class));
			s.register(5, LineShape.class, new Instantiator<>(LineShape.class));
			s.register(6, BlockFilter.class, new Instantiator<>(BlockFilter.class));
			s.register(7, BlockFilterLayer.class, new Instantiator<>(BlockFilterLayer.class));
			s.register(8, BlockDataWithConditions.class, new Instantiator<>(BlockDataWithConditions.class));
			s.register(9, DataCondition.class, new Instantiator<>(DataCondition.class));
			s.register(10, DataMetadata.class, new Instantiator<>(DataMetadata.class));
			s.register(11, ProjectIdentifier.class, new Instantiator<>(ProjectIdentifier.class));
			s.register(12, OrbisProject.class, new Instantiator<>(OrbisProject.class));
			s.register(13, DataIdentifier.class, new Instantiator<>(DataIdentifier.class));
			s.register(14, OrbisProjectCache.class, new Instantiator<>(OrbisProjectCache.class));
			s.register(15, ProjectMetadata.class, new Instantiator<>(ProjectMetadata.class));
			s.register(16, BlockDataContainerDefaultVoid.class, new Instantiator<>(BlockDataContainerDefaultVoid.class));
			s.register(17, CreationData.class, new Instantiator<>(CreationData.class));
			s.register(18, PyramidShape.class, new Instantiator<>(PyramidShape.class));
			s.register(19, ConeShape.class, new Instantiator<>(ConeShape.class));
			s.register(20, CylinderShape.class, new Instantiator<>(CylinderShape.class));
			s.register(21, DomeShape.class, new Instantiator<>(DomeShape.class));
			s.register(22, CuboidShape.class, new Instantiator<>(CuboidShape.class));
			s.register(23, DataCache.class, new Instantiator<>(DataCache.class));
			s.register(24, ScheduleLayer.class, new Instantiator<>(ScheduleLayer.class));
			s.register(25, FilterRecord.class, new Instantiator<>(FilterRecord.class));
			s.register(26, Entrance.class, new Instantiator<>(Entrance.class));
			s.register(27, BlueprintRegion.class, new Instantiator<>(BlueprintRegion.class));
			s.register(28, FrameworkNode.class, new Instantiator<>(FrameworkNode.class));
			s.register(29, ScheduleRegion.class, new Instantiator<>(ScheduleRegion.class));
			s.register(30, InventorySpawnEggs.class, new Instantiator<>(InventorySpawnEggs.class));
			s.register(31, ScheduleRecord.class, new Instantiator<>(ScheduleRecord.class));
			s.register(32, ScheduleBlueprint.class, new Instantiator<>(ScheduleBlueprint.class));
			s.register(33, BlockPosDimension.class, new Instantiator<>(BlockPosDimension.class));
			s.register(34, FilterOptions.class, new Instantiator<>(FilterOptions.class));
			s.register(35, FrameworkData.class, new Instantiator<>(FrameworkData.class));
			s.register(36, PlacedBlueprint.class, new Instantiator<>(PlacedBlueprint.class));
			s.register(37, PathwayData.class, new Instantiator<>(PathwayData.class));

			this.io.register(s);
		}

		return this.io;
	}

	@Nullable
	private IProject get(@Nullable final MinecraftServer server, final ResourceLocation projectPath, Object mod, String archiveBaseName)
	{
		final String s = projectPath.getResourcePath();

		if (this.loadedProjects.containsKey(s))
		{
			return this.loadedProjects.get(s);
		}
		else
		{
			if (server == null)
			{
				this.readProjectFromJar(mod, archiveBaseName, projectPath);
			}
			else
			{
				this.readProject(mod, archiveBaseName, projectPath);
			}

			return this.loadedProjects.getOrDefault(s, null);
		}
	}

	/**
	 * This reads a structure template from the given location and stores it.
	 * This first attempts get the template from an external folder.
	 * If it isn't there then it attempts to take it from the minecraft jar.
	 */
	private boolean readProject(Object mod, String archiveBaseName, final ResourceLocation server)
	{
		final String s = server.getResourcePath();
		final File file1 = new File(this.baseFolder, s + File.separator + "project_data.project");

		if (!file1.exists())
		{
			return this.readProjectFromJar(mod, archiveBaseName, server);
		}
		else
		{
			InputStream inputstream = null;
			boolean flag;

			try
			{
				inputstream = new FileInputStream(file1);
				this.readProjectFromStream(mod, archiveBaseName, s, inputstream, new File(this.baseFolder, s + "/").toURI());
				return true;
			}
			catch (final Throwable var10)
			{
				flag = false;
			}
			finally
			{
				IOUtils.closeQuietly(inputstream);
			}

			return flag;
		}
	}

	/**
	 * reads a template from the minecraft jar
	 */
	private boolean readProjectFromJar(Object mod, String archiveBaseName, final ResourceLocation id)
	{
		final String s = id.getResourceDomain();
		final String s1 = id.getResourcePath();
		InputStream inputstream = null;
		boolean flag;

		try
		{
			inputstream = MinecraftServer.class.getResourceAsStream("/assets/" + s + "/orbis/" + s1 + "/project_data.project");
			this.readProjectFromStream(mod, archiveBaseName, s1, inputstream, URI.create("/assets/" + s + "/orbis/" + s1 + "/"));
			return true;
		}
		catch (final IOException var10)
		{
			flag = false;
			this.log().error(var10);
		}
		finally
		{
			IOUtils.closeQuietly(inputstream);
		}

		return flag;
	}

	/**
	 * reads a template from an inputstream
	 */
	private void readProjectFromStream(Object mod, String archiveBaseName, final String id, final InputStream stream, final URI location) throws IOException
	{
		final NBTTagCompound tag = CompressedStreamTools.readCompressed(stream);

		final NBTFunnel funnel = new NBTFunnel(tag);

		final IProject project = funnel.get("project");

		project.setJarLocation(location);
		project.setModAndArchiveLoadingFrom(mod, archiveBaseName);

		project.loadAndCacheData();

		this.loadedProjects.put(id, project);
	}

	@Override
	public INetworkOrbis network()
	{
		return this.network;
	}

	@Override
	public void setNetwork(INetworkOrbis network)
	{
		this.network = network;
	}

	@Override
	public void listen(IOrbisServicesListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	@Override
	public boolean unlisten(IOrbisServicesListener listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public GameRegistrar registrar()
	{
		return this.gameRegistrar;
	}

	@Override
	public synchronized void startProjectManager()
	{
		if (this.projectManager != null)
		{
			return;
		}

		if (OrbisAPI.isClient())
		{
			final ServerData data = Minecraft.getMinecraft().getCurrentServerData();

			if (data != null)
			{
				this.projectManager = new OrbisProjectManager(
						new File(Minecraft.getMinecraft().mcDataDir, "/orbis/servers/" + data.serverIP.replace(":", "_") + "/projects/"), this.mod,
						this.archiveBaseName);
			}
			else
			{
				this.projectManager = new OrbisProjectManager(new File(Minecraft.getMinecraft().mcDataDir, "/orbis/local/projects/"), this.mod,
						this.archiveBaseName);
			}
		}

		if (this.projectManager == null)
		{
			this.projectManager = new OrbisProjectManager(new File(DimensionManager.getCurrentSaveRootDirectory(), "/orbis/projects/"), this.mod,
					this.archiveBaseName);
		}

		this.listeners.forEach(IOrbisServicesListener::onStartProjectManager);
	}

	@Override
	public synchronized void stopProjectManager()
	{
		if (this.projectManager != null)
		{
			this.projectManager.flushProjects();
			this.projectManager = null;
		}
	}

	@Override
	public IInstanceRegistry instances()
	{
		return this.instancesRegistry;
	}

	@Override
	public synchronized IProjectManager getProjectManager()
	{
		if (this.projectManager == null)
		{
			this.startProjectManager();
		}

		return this.projectManager;
	}

	@Override
	public void setProjectManagerInitSource(Object mod, String archiveBaseName)
	{
		this.mod = mod;
		this.archiveBaseName = archiveBaseName;
	}

}
