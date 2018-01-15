package com.gildedgames.orbis.common.capabilities.player;

import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class PlayerOrbisModule implements NBT
{
	private final PlayerOrbis playerOrbis;

	public PlayerOrbisModule(final PlayerOrbis playerAether)
	{
		this.playerOrbis = playerAether;
	}

	public abstract void onUpdate();

	public final PlayerOrbis getPlayer()
	{
		return this.playerOrbis;
	}

	public final EntityPlayer getEntity()
	{
		return this.playerOrbis.getEntity();
	}

	public final World getWorld()
	{
		return this.getEntity().world;
	}
}
