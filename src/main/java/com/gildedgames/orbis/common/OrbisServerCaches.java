package com.gildedgames.orbis.common;

import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.nbt.NBTTagCompound;

import java.util.concurrent.TimeUnit;

public class OrbisServerCaches
{

	private static final LoadingCache<NBTTagCompound, Optional<BlueprintDataPalette>> blueprintPaletteCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build(
					new CacheLoader<NBTTagCompound, Optional<BlueprintDataPalette>>()
					{
						@Override
						public Optional<BlueprintDataPalette> load(final NBTTagCompound tag)
						{
							final NBTFunnel funnel = new NBTFunnel(tag);

							final BlueprintDataPalette palette = funnel.get("palette");

							return Optional.of(palette);
						}
					});

	public static LoadingCache<NBTTagCompound, Optional<BlueprintDataPalette>> getBlueprintPalettes()
	{
		return blueprintPaletteCache;
	}

}
