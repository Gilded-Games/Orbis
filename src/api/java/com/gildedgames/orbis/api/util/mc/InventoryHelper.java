package com.gildedgames.orbis.api.util.mc;

import com.google.common.collect.Lists;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InventoryHelper
{

	public static List<ItemStack> getItemStacks(Slot[] slots)
	{
		final List<ItemStack> stacks = Lists.newArrayList();

		for (int i = 0; i < slots.length; i++)
		{
			final Slot slot = slots[i];

			if (slot.getStack() != null && !slot.getStack().isEmpty())
			{
				stacks.add(slot.getStack());
			}
		}

		return stacks;
	}

}
