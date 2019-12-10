package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis.common.network.packets.creation_settings.PacketSetPlaceChunksAsGhostRegions;
import com.gildedgames.orbis.common.network.packets.creation_settings.PacketSetPlacesAirBlocks;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerCreationSettingsModule extends PlayerOrbisModule
{

	private boolean placesAirBlocks, placeChunksAsGhostRegions;

	public PlayerCreationSettingsModule(final PlayerOrbis playerOrbis)
	{
		super(playerOrbis);
	}

	public boolean placesAirBlocks()
	{
		return this.placesAirBlocks;
	}

	public void setPlacesAirBlocks(boolean flag)
	{
		if (this.getWorld().isRemote)
		{
			OrbisCore.network().sendPacketToServer(new PacketSetPlacesAirBlocks(flag));
		}

		this.placesAirBlocks = flag;
	}

	public boolean placeChunksAsGhostRegions()
	{
		return this.placeChunksAsGhostRegions;
	}

	public void setPlaceChunksAsGhostRegions(boolean flag)
	{
		if (this.getWorld().isRemote)
		{
			OrbisCore.network().sendPacketToServer(new PacketSetPlaceChunksAsGhostRegions(flag));
		}

		this.placeChunksAsGhostRegions = flag;
	}

	@Override
	public void onUpdate()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setBoolean("placesAirBlocks", this.placesAirBlocks);
		tag.setBoolean("placeChunksAsGhostRegions", this.placeChunksAsGhostRegions);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.placesAirBlocks = tag.getBoolean("placesAirBlocks");
		this.placeChunksAsGhostRegions = tag.getBoolean("placeChunksAsGhostRegions");
	}
}
