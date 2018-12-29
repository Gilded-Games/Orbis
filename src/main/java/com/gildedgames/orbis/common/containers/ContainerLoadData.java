package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis.common.containers.inventory.InventoryBasicExpandable;
import com.gildedgames.orbis.common.containers.slots.SlotForge;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.gui.PacketBlueprintStackerInterface;
import com.gildedgames.orbis.player.IPlayerOrbis;
import com.gildedgames.orbis_api.client.gui.data.list.IListNavigator;
import com.gildedgames.orbis_api.client.gui.data.list.ListNavigator;
import com.gildedgames.orbis_api.util.mc.SlotHashed;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ContainerLoadData extends Container
{

	public final InventoryBasicExpandable stackerInventory = new InventoryBasicExpandable("tmp", true, 40)
	{
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack)
		{
			return stack.getItem() instanceof ItemBlueprint || stack.getItem() instanceof ItemBlueprintPalette;
		}
	};

	private final IPlayerOrbis playerOrbis;

	public SlotForge[] slots;

	private List<Slot> stackerSlots = Lists.newArrayList();

	private Slot topStackerSlot, bottomStackerSlot;

	private boolean displayedStacker;

	private IListNavigator<SlotHashed> navigator = new ListNavigator<>();

	public ContainerLoadData(final IPlayerOrbis playerOrbis, final IInventory forgeInventory)
	{
		this.playerOrbis = playerOrbis;

		this.slots = new SlotForge[4 * 4];

		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(playerOrbis.getEntity().inventory, j + i * 9 + 9, 213 + 7 + j * 18, 24 + 60 + 14 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new Slot(playerOrbis.getEntity().inventory, k, 32 + 181 + 7 + k * 18, 24 + 118 + 14));
		}

		for (int i = 0; i < 4; ++i)
		{
			for (int j = 0; j < 4; ++j)
			{
				final SlotForge slot = new SlotForge(forgeInventory, 0, (i * 4 + j), 32 + 187 + 15 + j * 18, 24 + -20 + i * 18);

				this.addSlotToContainer(slot);

				this.slots[i * 4 + j] = slot;
			}
		}
	}

	public IListNavigator<SlotHashed> getNavigator()
	{
		return this.navigator;
	}

	@Override
	public void putStackInSlot(int slotID, ItemStack stack)
	{
		super.putStackInSlot(slotID, stack);
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

	public void startStackerInterface()
	{
		if (!this.displayedStacker)
		{
			if (this.playerOrbis.getWorld().isRemote)
			{
				NetworkingOrbis.network().sendPacketToServer(new PacketBlueprintStackerInterface(true));
			}

			this.topStackerSlot = this.addSlotToContainer(new SlotHashed(this.stackerInventory, 38, 38, 0));
			this.bottomStackerSlot = this.addSlotToContainer(new SlotHashed(this.stackerInventory, 39, 38, 132 + 24));

			this.displayedStacker = true;
		}
	}

	public Slot getTopStackerSlot()
	{
		return this.topStackerSlot;
	}

	public Slot getBottomStackerSlot()
	{
		return this.bottomStackerSlot;
	}

	public void addStackerSlot(SlotHashed slot)
	{
		this.stackerSlots.add(this.addSlotToContainer(slot));
	}

	public int findPosition(Slot slot)
	{
		for (int i = 0; i < this.inventorySlots.size(); i++)
		{
			Slot s = this.inventorySlots.get(i);

			if (slot == s)
			{
				return i;
			}
		}

		return -1;
	}

	public void removeStackerSlot(SlotHashed slot)
	{
		if (this.stackerSlots.remove(slot))
		{
			int slotPos = this.findPosition(slot);

			this.inventorySlots.remove(slot);
			this.inventoryItemStacks.remove(slotPos);

			for (int i = slotPos; i < this.inventorySlots.size(); i++)
			{
				Slot s = this.inventorySlots.get(i);

				if (s instanceof SlotHashed)
				{
					SlotHashed bp = (SlotHashed) s;

					bp.oldY -= 24;

					bp.resetPos();
				}
			}
		}
	}

	public void display(List<SlotHashed> visible)
	{
		this.stopStackerInterface();
		this.startStackerInterface();

		for (int i = 0; i < visible.size(); i++)
		{
			SlotHashed slot = visible.get(i);

			slot.yPos = 30 + (i * 24);

			this.addStackerSlot(slot);
		}
	}

	public void stopStackerInterface()
	{
		if (this.displayedStacker)
		{
			if (this.playerOrbis.getWorld().isRemote)
			{
				NetworkingOrbis.network().sendPacketToServer(new PacketBlueprintStackerInterface(false));
			}

			this.inventorySlots.remove(this.topStackerSlot);
			this.inventorySlots.remove(this.bottomStackerSlot);

			this.inventoryItemStacks.remove(0);
			this.inventoryItemStacks.remove(0);

			for (int i = 0; i < this.stackerSlots.size(); i++)
			{
				Slot slot = this.stackerSlots.get(i);

				if (slot != null)
				{
					int slotPos = this.findPosition(slot);

					this.inventorySlots.remove(slot);
					this.inventoryItemStacks.remove(slotPos);

					if (slot instanceof SlotHashed)
					{
						SlotHashed bp = (SlotHashed) slot;

						bp.resetPos();
					}
				}
			}

			this.stackerSlots.clear();

			this.displayedStacker = false;
		}
	}

	@Override
	public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player)
	{
		return super.slotClick(slotId, dragType, clickTypeIn, player);
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

			if (index >= 27 && index < 36 && !this.mergeItemStack(itemstack1, 0, 27, false))
			{
				return ItemStack.EMPTY;
			}
			else if (index >= 36 && !this.mergeItemStack(itemstack1, 0, 36, false))
			{
				return ItemStack.EMPTY;
			}
			else if (!this.mergeItemStack(itemstack1, 27, 36, false))
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

			if (itemstack1.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer playerIn)
	{
		return true;
	}

}
