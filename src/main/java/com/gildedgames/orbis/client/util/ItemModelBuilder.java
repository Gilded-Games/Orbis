package com.gildedgames.orbis.client.util;

import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import java.util.HashMap;

public class ItemModelBuilder
{
	private final HashMap<Integer, ModelResourceLocation> registrations = new HashMap<>();

	private final String rootFolder;

	public ItemModelBuilder(String root)
	{
		if (!root.endsWith("/"))
		{
			throw new IllegalArgumentException("Path '" + root + "' isn't absolute");
		}

		this.rootFolder = root;
	}

	public ItemModelBuilder add(int meta, String path)
	{
		this.registrations.put(meta, new ModelResourceLocation(OrbisCore.getPath(
				this.rootFolder != null ? this.rootFolder + path : path), "inventory"));

		return this;
	}

	public HashMap<Integer, ModelResourceLocation> getRegistrations()
	{
		return this.registrations;
	}
}
