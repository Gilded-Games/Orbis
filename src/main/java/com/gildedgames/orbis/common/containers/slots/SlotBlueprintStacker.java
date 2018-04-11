package com.gildedgames.orbis.common.containers.slots;

import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SlotBlueprintStacker extends Slot
{

	private final int indexOffset;

	public int oldX, oldY;

	public SlotBlueprintStacker(final IInventory inventory, final int indexOffset, final int index, final int xPosition, final int yPosition)
	{
		super(inventory, index, xPosition, yPosition);

		this.oldX = xPosition;
		this.oldY = yPosition;

		this.indexOffset = indexOffset;
	}

	public void resetPos()
	{
		this.xPos = this.oldX;
		this.yPos = this.oldY;
	}

	@Override
	public ItemStack decrStackSize(final int amount)
	{
		return this.inventory.decrStackSize(this.getSlotIndex() - this.indexOffset, amount);
	}

	@Override
	public boolean isHere(final IInventory inv, final int slotIn)
	{
		return inv == this.inventory && slotIn == this.getSlotIndex();
	}

	@Override
	public ItemStack getStack()
	{
		return this.inventory.getStackInSlot(this.getSlotIndex() - this.indexOffset);
	}

	@Override
	public void putStack(final ItemStack stack)
	{
		this.inventory.setInventorySlotContents(this.getSlotIndex() - this.indexOffset, stack);
		this.onSlotChanged();
	}

	@Override
	public boolean isItemValid(final ItemStack stack)
	{
		return this.inventory.isItemValidForSlot(this.getSlotIndex() - this.indexOffset, stack) && (stack.getItem() instanceof ItemBlueprint || stack
				.getItem() instanceof ItemBlueprintPalette);
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.getSlotIndex());

		return builder.toHashCode();
	}
}
