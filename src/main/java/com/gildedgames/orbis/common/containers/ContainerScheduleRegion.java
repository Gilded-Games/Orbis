package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis.common.capabilities.player.IPlayerOrbis;
import com.gildedgames.orbis.common.containers.slots.SlotForge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerScheduleRegion extends ContainerPlayer
{
	private final IPlayerOrbis playerOrbis;

	public SlotForge[] slots;

	public ContainerScheduleRegion(final IPlayerOrbis playerOrbis, final IInventory forgeInventory)
	{
		super(playerOrbis.getEntity().inventory, false, playerOrbis.getEntity());

		this.playerOrbis = playerOrbis;

		this.createSlots(forgeInventory);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < this.inventorySlots.size(); ++i)
		{
			final ItemStack itemstack = this.inventorySlots.get(i).getStack();

			if (!itemstack.isEmpty())
			{
				itemstack.getItem().onUpdate(itemstack, this.playerOrbis.getEntity().getEntityWorld(), this.playerOrbis.getEntity(), i, false);
			}
		}
	}

	private void createSlots(final IInventory forgeInventory)
	{
		final int widthOffset = 6;
		final int heightOffset = 7;

		for (final Slot slot : this.inventorySlots)
		{
			slot.xPos += widthOffset;
			slot.yPos += heightOffset;
		}

		final Slot helmet = this.inventorySlots.get(5);
		final Slot chestplate = this.inventorySlots.get(6);
		final Slot leggings = this.inventorySlots.get(7);
		final Slot boots = this.inventorySlots.get(8);
		final Slot shield = this.inventorySlots.get(45);

		helmet.xPos = helmet.yPos = -2000;
		chestplate.xPos = chestplate.yPos = -2000;
		leggings.xPos = leggings.yPos = -2000;
		boots.xPos = boots.yPos = -2000;
		shield.xPos = shield.yPos = -2000;

		final Slot craftResult = this.inventorySlots.get(0);

		final Slot craft1 = this.inventorySlots.get(1);
		final Slot craft2 = this.inventorySlots.get(2);
		final Slot craft3 = this.inventorySlots.get(3);
		final Slot craft4 = this.inventorySlots.get(4);

		craft1.xPos = craft1.yPos = -2000;
		craft2.xPos = craft2.yPos = -2000;
		craft3.xPos = craft3.yPos = -2000;
		craft4.xPos = craft4.yPos = -2000;

		craftResult.xPos = craftResult.yPos = -2000;

		this.slots = new SlotForge[4 * 4];

		final int indexOffset = this.inventorySlots.size();

		for (int i = 0; i < 4; ++i)
		{
			for (int j = 0; j < 4; ++j)
			{
				final SlotForge slot = new SlotForge(forgeInventory, indexOffset, indexOffset + (i * 4 + j), j * 18, i * 18);

				this.addSlotToContainer(slot);

				this.slots[i * 4 + j] = slot;
			}
		}
	}

	@Override
	public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player)
	{
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer player, final int slotNumber)
	{
		return super.transferStackInSlot(player, slotNumber);
	}

	@Override
	public boolean canInteractWith(final EntityPlayer playerIn)
	{
		return true;
	}
}
