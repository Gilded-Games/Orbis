package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis.common.network.packets.creation_settings.PacketSetPlacesAirBlocks;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerCreationSettingsModule extends PlayerOrbisModule
{

	private boolean placesAirBlocks;

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

	@Override
	public void onUpdate()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setBoolean("placesAirBlocks", this.placesAirBlocks);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.placesAirBlocks = tag.getBoolean("placesAirBlocks");
	}
}
