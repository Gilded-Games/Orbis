package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.client.godmode.GodPowerScheduleClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GodPowerSchedule implements IGodPower
{

	private final ShapeSelectorFilter shapeSelector;

	private GodPowerScheduleClient clientHandler;

	public GodPowerSchedule(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerScheduleClient(this);
		}

		this.shapeSelector = new ShapeSelectorFilter(p -> new BlockFilter(BlockFilterHelper.getNewFillLayer(p.getHeldItemMainhand())), false);
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
		return false;
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
