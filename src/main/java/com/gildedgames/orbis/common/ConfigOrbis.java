package com.gildedgames.orbis.common;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ConfigOrbis
{
	public final ConfigCategory biomes, dimensions;

	private final Configuration configuration;

	private int orbisDimId;

	public ConfigOrbis(File file)
	{
		this.configuration = new Configuration(file, true);

		this.biomes = this.configuration.getCategory("Biome IDs");
		this.dimensions = this.configuration.getCategory("Dimension IDs");

		this.loadAndSync();
	}

	private void loadAndSync()
	{
		this.orbisDimId = this.getInt(this.dimensions, "Orbis Dimension ID", 4);

		if (this.configuration.hasChanged())
		{
			this.configuration.save();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(OrbisCore.MOD_ID))
		{
			this.loadAndSync();
		}
	}

	private int getInt(ConfigCategory category, String name, int defaultValue)
	{
		return this.configuration.get(category.getName(), name, defaultValue).getInt();
	}

	private boolean getBoolean(ConfigCategory category, String name, boolean defaultValue)
	{
		return this.configuration.get(category.getName(), name, defaultValue).getBoolean();
	}

	public int getOrbisDimId()
	{
		return this.orbisDimId;
	}
}

