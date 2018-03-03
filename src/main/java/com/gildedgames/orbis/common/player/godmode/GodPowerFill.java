package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.inventory.InventoryBlockForge;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.api.util.mc.StagedInventory;
import com.gildedgames.orbis.client.godmode.GodPowerFillClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GodPowerFill implements IGodPower
{

	private final ShapeSelectorFilter shapeSelector;

	private final StagedInventory<InventoryBlockForge> stagedInventory;

	private GodPowerFillClient clientHandler = null;

	public GodPowerFill(final PlayerOrbis playerOrbis, final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerFillClient(this);
		}

		this.shapeSelector = new ShapeSelectorFilter(p -> new BlockFilter(BlockFilterHelper.getNewFillLayer(p.getHeldItemMainhand())), false);
		this.stagedInventory = new StagedInventory<>(playerOrbis.getEntity(), () -> new InventoryBlockForge(playerOrbis.getEntity()),
				m -> PlayerOrbis.get(m).powers().getFillPower().getStagedInventory(), "blockForge");
	}

	public StagedInventory<InventoryBlockForge> getStagedInventory()
	{
		return this.stagedInventory;
	}

	public IInventory getForgeInventory()
	{
		return this.stagedInventory.get();
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
	public boolean hasCustomGui(PlayerOrbis playerOrbis)
	{
		return true;
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty();
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
