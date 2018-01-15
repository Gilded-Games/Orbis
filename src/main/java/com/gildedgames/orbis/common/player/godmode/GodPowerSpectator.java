package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerSpectatorClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorInvalid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GodPowerSpectator implements IGodPower
{

	private final IShapeSelector shapeSelector;

	private GodPowerSpectatorClient clientHandler;

	public GodPowerSpectator(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerSpectatorClient(this);
		}

		this.shapeSelector = new ShapeSelectorInvalid();
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Override
	public void onUpdate(final EntityPlayer player, final PlayerOrbis playerOrbis, final boolean isPowerActive)
	{
		if (isPowerActive)
		{
			player.setGameType(GameType.SPECTATOR);
		}
		else if (playerOrbis.inDeveloperMode() && !player.isCreative())
		{
			player.setGameType(GameType.CREATIVE);
		}
	}

	@Override
	public boolean hasCustomGui()
	{
		return false;
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		return true;
	}

	@Nullable
	@Override
	public IShapeSelector getShapeSelector()
	{
		return this.shapeSelector;
	}

	@Override
	public IGodPowerClient getClientHandler()
	{
		return this.clientHandler;
	}
}
