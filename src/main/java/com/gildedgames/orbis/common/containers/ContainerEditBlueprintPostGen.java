package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis.common.containers.inventory.InventoryBasicExpandable;
import com.gildedgames.orbis.common.items.ItemBlockPalette;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.player.IPlayerOrbis;
import com.gildedgames.orbis_api.client.gui.data.list.IListNavigator;
import com.gildedgames.orbis_api.client.gui.data.list.ListNavigator;
import com.gildedgames.orbis_api.data.schedules.PostGenReplaceLayer;
import com.gildedgames.orbis_api.util.mc.SlotHashed;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public class ContainerEditBlueprintPostGen extends Container
{

	public final InventoryBasicExpandable stackerInventory = new InventoryBasicExpandable("tmp", true, 3)
	{
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack)
		{
			return ItemBlock.class.isAssignableFrom(stack.getItem().getClass()) || stack.getItem() instanceof ItemBucket || ItemMultiTexture.class
					.isAssignableFrom(stack.getItem().getClass()) || ItemBlockPalette.class.isAssignableFrom(stack.getItem().getClass());
		}
	};

	private final IPlayerOrbis playerOrbis;

	private List<SlotGroup> layers = Lists.newArrayList();

	private boolean displayedLayers;

	private IListNavigator<SlotGroup> navigator = new ListNavigator<>();

	private Blueprint blueprint;

	public ContainerEditBlueprintPostGen(final IPlayerOrbis playerOrbis, Blueprint blueprint)
	{
		this.playerOrbis = playerOrbis;
		this.blueprint = blueprint;

		List<Runnable> calls = Lists.newArrayList();

		for (Map.Entry<Integer, PostGenReplaceLayer> e : this.blueprint.getData().getPostGenReplaceLayers().entrySet())
		{
			int i = e.getKey();
			PostGenReplaceLayer layer = e.getValue();

			// To prevent concurrent modification exception
			calls.add(() ->
			{
				this.stackerInventory.expand((i * 2) + 37 + 2);

				SlotHashed slot1 = new SlotHashed(this.stackerInventory, (i * 2) + 37, 6, 6 + ((i * 24) % 120));
				SlotHashed slot2 = new SlotHashed(this.stackerInventory, (i * 2) + 37 + 1, 37, 6 + ((i * 24) % 120));

				Pair<SlotHashed, SlotHashed> pair = Pair.of(slot1, slot2);

				this.getNavigator().put(new SlotGroup(pair, i), i, false);

				this.stackerInventory.setInventorySlotContents((i * 2) + 37, layer.getRequired());
				this.stackerInventory.setInventorySlotContents((i * 2) + 37 + 1, layer.getReplaced());
			});
		}

		calls.forEach(Runnable::run);
		calls.clear();

		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(playerOrbis.getEntity().inventory, j + i * 9 + 9, 186 + 7 + j * 18, 19 + 14 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new Slot(playerOrbis.getEntity().inventory, k, 186 + 7 + k * 18, 77 + 14));
		}

		this.startLayerInterface();
	}

	public IListNavigator<SlotGroup> getNavigator()
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

	public void startLayerInterface()
	{
		if (!this.displayedLayers)
		{
			this.displayedLayers = true;
		}
	}

	public void addLayer(SlotGroup layer)
	{
		this.addSlotToContainer(layer.getSlots().getLeft());
		this.addSlotToContainer(layer.getSlots().getRight());

		this.layers.add(layer);
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

	public void removeLayer(SlotGroup layer)
	{
		if (this.layers.remove(layer))
		{
			int slotPos = this.findPosition(layer.getSlots().getLeft());

			this.inventorySlots.remove(layer.getSlots().getLeft());
			this.inventoryItemStacks.remove(slotPos);

			slotPos = this.findPosition(layer.getSlots().getRight());

			this.inventorySlots.remove(layer.getSlots().getRight());
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

	public void display(List<SlotGroup> visible)
	{
		this.stopStackerInterface();
		this.startLayerInterface();

		for (int i = 0; i < visible.size(); i++)
		{
			SlotGroup layer = visible.get(i);

			layer.getSlots().getLeft().yPos = 6 + (i * 24);
			layer.getSlots().getRight().yPos = 6 + (i * 24);

			this.addLayer(layer);
		}
	}

	public void stopStackerInterface()
	{
		if (this.displayedLayers)
		{
			for (int i = 0; i < this.layers.size(); i++)
			{
				SlotGroup layer = this.layers.get(i);

				if (layer != null)
				{
					int slotPos = this.findPosition(layer.getSlots().getLeft());

					this.inventorySlots.remove(layer.getSlots().getLeft());
					this.inventoryItemStacks.remove(slotPos);

					SlotHashed bp = layer.getSlots().getLeft();
					bp.resetPos();

					slotPos = this.findPosition(layer.getSlots().getRight());

					this.inventorySlots.remove(layer.getSlots().getRight());
					this.inventoryItemStacks.remove(slotPos);

					bp = layer.getSlots().getRight();
					bp.resetPos();
				}
			}

			this.layers.clear();

			this.displayedLayers = false;
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
