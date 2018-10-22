package com.gildedgames.orbis.common;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ConfigOrbis
{
	public final ConfigCategory biomes, dimensions, misc;

	private final Configuration configuration;

	private int orbisDimId;

	private boolean seenAlphaNotice;

	private boolean useExperimentalFeatures;

	public ConfigOrbis(File file)
	{
		this.configuration = new Configuration(file, true);

		this.biomes = this.configuration.getCategory("Biome IDs");
		this.dimensions = this.configuration.getCategory("Dimension IDs");
		this.misc = this.configuration.getCategory("Miscellaneous");

		this.loadAndSync();
	}

	private void loadAndSync()
	{
		this.orbisDimId = this.getInt(this.dimensions, "Orbis Dimension ID", 4);
		this.seenAlphaNotice = this.getBoolean(this.misc, "Has Seen Alpha Notice", false);
		this.useExperimentalFeatures = this.getBoolean(this.misc, "Use Experimental Features", false);

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

	public boolean useExperimentalFeatures()
	{
		return this.useExperimentalFeatures;
	}

	public int getOrbisDimId()
	{
		return this.orbisDimId;
	}

	public boolean hasSeenAlphaNotice()
	{
		return this.seenAlphaNotice;
	}

	public void markSeenAlphaNotice()
	{
		this.seenAlphaNotice = true;

		this.configuration.get(this.misc.getName(), "Has Seen Alpha Notice", false).set(true);

		if (this.configuration.hasChanged())
		{
			this.configuration.save();
		}
	}
}

