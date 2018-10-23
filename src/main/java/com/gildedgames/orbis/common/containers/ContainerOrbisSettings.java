package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis.common.containers.slots.SlotForge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerOrbisSettings extends Container
{
	public SlotForge orbisFloorSlot;

	public ContainerOrbisSettings(final InventoryPlayer playerInventory, IInventory orbisFloorInventory)
	{
		final SlotForge slot = new SlotForge(orbisFloorInventory, 0, 0, 33, 84);

		this.addSlotToContainer(slot);

		this.orbisFloorSlot = slot;

		// player inventory
		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 129 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 187));
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull final EntityPlayer playerIn)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		final Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			final ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 1)
			{
				if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			else if (!this.mergeItemStack(itemstack1, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

}
