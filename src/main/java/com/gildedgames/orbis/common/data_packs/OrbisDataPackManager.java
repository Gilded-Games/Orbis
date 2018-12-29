package com.gildedgames.orbis.common.data_packs;

import com.gildedgames.orbis.OrbisAPI;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.packs.IOrbisPack;
import com.gildedgames.orbis.packs.IOrbisPackData;
import com.gildedgames.orbis_api.OrbisLib;
import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OrbisDataPackManager
{
	public static final String EXTENSION = "orbispack";

	private final File location;

	private Map<UUID, IOrbisPackData> packData = Maps.newHashMap();

	public OrbisDataPackManager(File location)
	{
		if (!location.exists() && !location.mkdirs())
		{
			throw new RuntimeException("Directory for OrbisDataPackManager cannot be created!");
		}

		if (!location.isDirectory())
		{
			throw new IllegalArgumentException("File passed into OrbisDataPackManager is not a directory!");
		}

		this.location = location;
	}

	public File getBaseDirectory()
	{
		return this.location;
	}

	private void read()
	{
		try (Stream<Path> paths = Files.walk(Paths.get(this.location.getPath())))
		{
			paths.forEach(p ->
			{
				final File file = p.toFile();

				final String extension = FilenameUtils.getExtension(file.getName());

				/** Prevents the path walking from including non-cache files **/
				if (!extension.equals(EXTENSION))
				{
					return;
				}

				try
				{
					ZipFile zipFile = new ZipFile(file);

					ZipEntry entry = zipFile.getEntry("pack.meta");

					if (entry != null)
					{
						try (InputStream inputStream = zipFile.getInputStream(entry); InputStreamReader reader = new InputStreamReader(inputStream))
						{
							try
							{
								IOrbisPack pack = OrbisAPI.services().getGson().fromJson(reader, IOrbisPack.class);

								for (IOrbisPackData data : pack.getData())
								{
									data.assembleDependencies(zipFile);

									this.packData.put(data.getUniqueId(), data);
								}
							}
							catch (JsonSyntaxException | JsonIOException | IOException e)
							{
								OrbisLib.LOGGER.error("Failed to load pack.meta file from Orbis Pack", e);
							}
						}
					}

					zipFile.close();

					String relativeDestination = file.getPath().replace(this.location.getPath(), "");

					//TODO:
					//OrbisCore.PROXY.getFTT().transfer(UUID.randomUUID(), file, relativeDestination, );
				}
				catch (IOException e)
				{
					OrbisCore.LOGGER.info("Orbis Pack is not in proper zip format", p);
				}
			});
		}
		catch (final IOException e)
		{
			OrbisLib.LOGGER.error(e);
		}
	}

	public Collection<IOrbisPackData> getPackData()
	{
		return this.packData.values();
	}

	public void start()
	{
		this.read();
	}

	public void stop()
	{
		this.packData = null;
	}
}
