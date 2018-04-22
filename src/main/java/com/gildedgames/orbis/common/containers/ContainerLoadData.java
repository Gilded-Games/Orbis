package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis_api.client.gui.data.list.IListNavigator;
import com.gildedgames.orbis_api.client.gui.data.list.ListNavigator;
import com.gildedgames.orbis.common.capabilities.player.IPlayerOrbis;
import com.gildedgames.orbis.common.containers.inventory.InventoryBasicExpandable;
import com.gildedgames.orbis.common.containers.slots.SlotBlueprintStacker;
import com.gildedgames.orbis.common.containers.slots.SlotForge;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ContainerLoadData extends ContainerPlayer
{

	private static final InventoryBasic dumbInventory = new InventoryBasic("tmp", true, 52);

	public final InventoryBasicExpandable stackerInventory = new InventoryBasicExpandable("tmp", true, 3);

	private final IPlayerOrbis playerOrbis;

	public SlotForge[] slots;

	private List<Slot> stackerSlots = Lists.newArrayList();

	private Slot topStackerSlot, bottomStackerSlot;

	private boolean displayedStacker;

	private IListNavigator<SlotBlueprintStacker> navigator = new ListNavigator<>();

	public ContainerLoadData(final IPlayerOrbis playerOrbis, final IInventory forgeInventory)
	{
		super(playerOrbis.getEntity().inventory, false, playerOrbis.getEntity());

		this.playerOrbis = playerOrbis;

		this.createSlots(forgeInventory);

		this.startStackerInterface();
	}

	public IListNavigator<SlotBlueprintStacker> getNavigator()
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
			this.topStackerSlot = this.addSlotToContainer(new SlotBlueprintStacker(this.stackerInventory, 40, 41, 6, -24));
			this.bottomStackerSlot = this.addSlotToContainer(new SlotBlueprintStacker(this.stackerInventory, 40, 42, 6, 132));

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

	public void addStackerSlot(SlotBlueprintStacker slot)
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

	public void removeStackerSlot(SlotBlueprintStacker slot)
	{
		if (this.stackerSlots.remove(slot))
		{
			int slotPos = this.findPosition(slot);

			this.inventorySlots.remove(slot);
			this.inventoryItemStacks.remove(slotPos);

			for (int i = slotPos; i < this.inventorySlots.size(); i++)
			{
				Slot s = this.inventorySlots.get(i);

				if (s instanceof SlotBlueprintStacker)
				{
					SlotBlueprintStacker bp = (SlotBlueprintStacker) s;

					bp.oldY -= 24;

					bp.resetPos();
				}
			}
		}
	}

	public void display(List<SlotBlueprintStacker> visible)
	{
		this.stopStackerInterface();
		this.startStackerInterface();

		for (int i = 0; i < visible.size(); i++)
		{
			SlotBlueprintStacker slot = visible.get(i);

			slot.yPos = 6 + (i * 24);

			this.addStackerSlot(slot);
		}
	}

	public void stopStackerInterface()
	{
		if (this.displayedStacker)
		{
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

					if (slot instanceof SlotBlueprintStacker)
					{
						SlotBlueprintStacker bp = (SlotBlueprintStacker) slot;

						bp.resetPos();
					}
				}
			}

			this.stackerSlots.clear();

			this.displayedStacker = false;
		}
	}

	private void createSlots(final IInventory forgeInventory)
	{
		final int widthOffset = 180;
		final int heightOffset = -10;

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
				final SlotForge slot = new SlotForge(forgeInventory, indexOffset, indexOffset + (i * 4 + j), 187 + 15 + j * 18, -20 + i * 18);

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
