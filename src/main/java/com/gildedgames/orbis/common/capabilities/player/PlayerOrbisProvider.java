package com.gildedgames.orbis.common.capabilities.player;

import com.gildedgames.orbis.common.OrbisCapabilities;
import com.gildedgames.orbis.player.IPlayerOrbis;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerOrbisProvider implements ICapabilitySerializable<NBTBase>
{
	private final PlayerOrbis.Storage storage = new PlayerOrbis.Storage();

	private final IPlayerOrbis aePlayer;

	public PlayerOrbisProvider(IPlayerOrbis aePlayer)
	{
		this.aePlayer = aePlayer;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == OrbisCapabilities.PLAYER_DATA && this.aePlayer != null;
	}

	@Override
	@SuppressWarnings("unchecked" /* joy... */)
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (this.hasCapability(capability, facing))
		{
			return (T) this.aePlayer;
		}

		return null;
	}

	@Override
	public NBTBase serializeNBT()
	{
		return this.storage.writeNBT(OrbisCapabilities.PLAYER_DATA, this.aePlayer, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		this.storage.readNBT(OrbisCapabilities.PLAYER_DATA, this.aePlayer, null, nbt);
	}
}
