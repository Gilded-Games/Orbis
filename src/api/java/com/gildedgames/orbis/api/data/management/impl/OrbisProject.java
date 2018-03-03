package com.gildedgames.orbis.api.data.management.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.management.*;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class OrbisProject implements IProject
{
	private final List<IProjectListener> listeners = Lists.newArrayList();

	private final List<String> acceptedFileExtensions = Lists.newArrayList();

	private IProjectCache cache;

	private URI jarLocation;

	private File locationFile;

	private IProjectIdentifier identifier;

	private IProjectMetadata metadata;

	private Object mod;

	private String archiveBaseName;

	private OrbisProject()
	{
		this.cache = new OrbisProjectCache(this);

		this.acceptedFileExtensions.add("blueprint");
	}

	/**
	 * Should be used to load back an existing project.
	 * @param location
	 */
	private OrbisProject(final File location)
	{
		this();

		this.jarLocation = location.toURI();
		this.locationFile = location;
	}

	/**
	 * Should be used to createTE a new project rather than an existing one.
	 * @param location The jarLocation of the project.
	 * @param identifier The unique identifier for the project.
	 */
	public OrbisProject(final File location, final IProjectIdentifier identifier)
	{
		this();

		if (!location.exists() && !location.mkdirs())
		{
			throw new RuntimeException("Location for OrbisProject cannot be created!");
		}

		if (!location.isDirectory())
		{
			throw new IllegalArgumentException("Location file passed into OrbisProject is not a directory!");
		}

		this.jarLocation = location.toURI();
		this.locationFile = location;

		this.identifier = identifier;

		this.metadata = new ProjectMetadata();
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("nextDataId", this.cache.getNextDataId());

		funnel.set("identifier", this.identifier);
		funnel.set("metadata", this.metadata);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.cache.setNextDataId(tag.getInteger("nextDataId"));

		this.identifier = funnel.get("identifier");
		this.metadata = funnel.get("metadata");
	}

	@Nullable
	@Override
	public File getLocationAsFile()
	{
		return this.locationFile;
	}

	@Override
	public void setLocationAsFile(final File location)
	{
		this.locationFile = location;
	}

	@Override
	public URI getJarLocation()
	{
		return this.jarLocation;
	}

	@Override
	public void setJarLocation(final URI uri)
	{
		this.jarLocation = uri;
	}

	@Override
	public void addListener(final IProjectListener listener)
	{
		this.listeners.add(listener);
	}

	@Override
	public boolean removeListener(final IProjectListener listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public IProjectIdentifier getProjectIdentifier()
	{
		return this.identifier;
	}

	@Override
	public IProjectMetadata getMetadata()
	{
		return this.metadata;
	}

	@Override
	public IProjectCache getCache()
	{
		return this.cache;
	}

	@Override
	public void setCache(final IProjectCache cache)
	{
		cache.setProject(this);

		this.cache = cache;
	}

	@Override
	public void writeData(final IData data, final File file)
	{
		try (FileOutputStream out = new FileOutputStream(file))
		{
			final NBTTagCompound tag = new NBTTagCompound();
			final NBTFunnel funnel = new NBTFunnel(tag);

			funnel.set("data", data);

			CompressedStreamTools.writeCompressed(tag, out);
		}
		catch (final IOException e)
		{
			OrbisAPI.services().log().error("Failed to save project data to disk", e);
		}
	}

	@Override
	public void loadData(IData data, File file, String location)
	{
		final boolean fromOtherProject = !this.identifier.equals(data.getMetadata().getIdentifier().getProjectIdentifier());

		/* If the data file seems to be moved from another project, it'll reassign a new data id for it **/
		if (fromOtherProject)
		{
			data.getMetadata().setIdentifier(this.cache.createNextIdentifier());
		}

		/*
		 * This will determine if a new identifier will be created
		 * when the data is null.
		 */
		final boolean shouldSaveAfter = data.getMetadata().getIdentifier() == null;

		/* Loads data from file then sets it to the cache **/
		this.cache.setData(data, location);

		/*
		 * Save the data to disk to ensure it doesn't keep creating
		 * new identifiers each time the project is loaded.
		 */
		if (this.locationFile != null && (shouldSaveAfter || fromOtherProject) && !OrbisAPI.isClient())
		{
			this.writeData(data, file);
		}
	}

	@Override
	public boolean findAndLoadData(IDataIdentifier id)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("Identifier should not be null when trying to find and load data!");
		}

		final boolean[] found = { false };

		this.walkDataLoading((nbt, location, file, uri) -> {
			// If already loaded, continue to next file
			if (this.cache.getDataId(location) != -1)
			{
				return;
			}

			final IData data = nbt.loadWithoutReading("data");

			if (data != null)
			{
				// Nested data tags because of the way NBTHelper writes NBT files
				NBTTagCompound tag = nbt.getTag().getCompoundTag("data").getCompoundTag("data");

				data.readMetadataOnly(tag);

				if (id.equals(data.getMetadata().getIdentifier()))
				{
					found[0] = true;

					data.read(tag);

					this.loadData(data, file, location);
				}
			}
		});

		return found[0];
	}

	@Override
	public void setModAndArchiveLoadingFrom(Object mod, String archiveBaseName)
	{
		this.mod = mod;
		this.archiveBaseName = archiveBaseName;
	}

	/**
	 * Walks through the project finding data files, then calls the provided
	 * consumer to do what you want with that data file.
	 */
	private void walkDataLoading(ProjectDataWalker dataWalker)
	{
		try
		{
			/*
			 * Uses the file jarLocation if accessible (used on Orbis client)
			 * Otherwise, uses URI and accesses from MC server resource
			 * so it works when stored in a mod jar.
			 */
			final String rawPath = this.mod.getClass().getResource("").toURI().toString();
			URI resources = URI.create(rawPath);

			final Path myPath;
			FileSystem fileSystem = null;

			final boolean usesJar;

			String modPackage = "/" + this.mod.getClass().getName().replace(this.mod.getClass().getSimpleName(), "").replace(".", "/");

			/* INSIDE JAR **/
			if (resources.getScheme().equals("jar"))
			{
				resources = URI.create(rawPath.replace(modPackage, "/"));

				fileSystem = FileSystems.newFileSystem(resources, Collections.<String, Object>emptyMap());
				myPath = fileSystem.getPath("/");

				usesJar = true;
			}
			else if (this.jarLocation != null) /* DEVELOPMENT WORKSPACE, JAR **/
			{
				String subRaw = rawPath.substring(rawPath.lastIndexOf(this.archiveBaseName));
				String pack = subRaw.substring(this.archiveBaseName.length(), subRaw.indexOf("/"));

				String orig = "/" + this.archiveBaseName + pack + modPackage;
				String assets = "/" + this.archiveBaseName + "_main/assets/";

				resources = URI.create(rawPath.replace(orig, assets));

				myPath = Paths.get(resources);

				usesJar = false;
			}
			else
			{
				myPath = Paths.get(this.locationFile.getPath());

				usesJar = false;
			}

			try (Stream<Path> paths = Files.walk(myPath))
			{
				paths.forEach(p ->
				{
					final URI uri = p.toUri();
					final String path = uri.toString().contains("!") ? uri.toString().split("!")[1] : uri.toString();

					/* Prevents the path walking from including the project's jarLocation **/
					if (path == null || path.equals(this.locationFile != null ? this.locationFile.getPath() : this.jarLocation.getPath()))
					{
						return;
					}

					final String extension = FilenameUtils.getExtension(path);

					/* Prevents the path walking from including the project data itself (hidden file) **/
					if (extension.equals("project"))
					{
						return;
					}

					/* Make sure the file has an extension type accepted by this project **/
					if (this.acceptedFileExtensions.contains(extension))
					{
						try
						{
							File file = null;

							if (!usesJar)
							{
								file = new File(uri);
							}

							final String loc = !usesJar ? file.getCanonicalPath() : path;

							try (InputStream in = usesJar ? MinecraftServer.class.getResourceAsStream(loc) : new FileInputStream(file))
							{
								final NBTTagCompound tag = CompressedStreamTools.readCompressed(in);

								final NBTFunnel funnel = new NBTFunnel(tag);

								final String location = loc.replace(
										loc.substring(0, loc.lastIndexOf(this.identifier.getProjectId()) + this.identifier.getProjectId().length() + 1),
										"");

								dataWalker.walk(funnel, location, file, uri);
							}
							catch (final IOException e)
							{
								OrbisAPI.services().log().error(e);
							}
						}
						catch (final IOException e)
						{
							OrbisAPI.services().log().error(e);
						}
					}
				});
			}
			catch (final IOException e)
			{
				OrbisAPI.services().log().error(e);
			}

			if (fileSystem != null)
			{
				fileSystem.close();
			}
		}
		catch (final IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void loadAndCacheData()
	{
		this.walkDataLoading((nbt, location, file, uri) -> {
			final IData data = nbt.get("data");

			if (data != null && this.cache.getDataId(location) == -1)
			{
				this.loadData(data, file, location);
			}
			else
			{
				OrbisAPI.services().log().error("Failed to load back a data file from project.", uri);
			}
		});
	}

	@Override
	public boolean areModDependenciesMet()
	{
		return true;
	}

	private interface ProjectDataWalker
	{
		void walk(NBTFunnel funnel, String location, File file, URI uri);
	}
}
