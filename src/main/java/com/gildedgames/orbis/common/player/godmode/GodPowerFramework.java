package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerFrameworkClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorFramework;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class GodPowerFramework implements IGodPower
{

	private final ShapeSelectorFramework shapeSelector;

	private GodPowerFrameworkClient clientHandler;

	public GodPowerFramework(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerFrameworkClient();
		}

		this.shapeSelector = new ShapeSelectorFramework(this);
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
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty() && (held.getItem() instanceof ItemBlock
				|| held.getItem() instanceof ItemBlueprint
				|| held.getItem() instanceof ItemBlockDataContainer);
	}

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

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

}
