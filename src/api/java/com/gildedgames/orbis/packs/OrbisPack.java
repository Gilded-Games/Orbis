package com.gildedgames.orbis.packs;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class OrbisPack implements IOrbisPack
{
	@SerializedName("pack_name")
	private String packName;

	@SerializedName("pack_icon_location")
	private String packIconLocation;

	@SerializedName("data")
	private Collection<IOrbisPackData> data;

	@Override
	@Nonnull
	public String getPackName()
	{
		return this.packName;
	}

	@Override
	@Nonnull
	public String getPackIconLocation()
	{
		return this.packIconLocation;
	}

	@Override
	@Nonnull
	public Collection<IOrbisPackData> getData()
	{
		return this.data == null ? Collections.emptyList() : this.data;
	}
}
