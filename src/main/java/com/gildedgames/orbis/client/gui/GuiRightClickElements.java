package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.gui.data.DropdownElement;
import com.gildedgames.orbis.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketFilterShape;
import com.gildedgames.orbis.common.network.packets.PacketSetBlockDataContainerInHand;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

public class GuiRightClickElements
{
	public static DropdownElement remove(final IWorldObject region)
	{
		return new DropdownElement(new TextComponentString("Remove"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				final WorldObjectManager manager = WorldObjectManager.get(player.world);
				NetworkingOrbis.sendPacketToServer(new PacketWorldObjectRemove(region.getWorld(), manager.getGroup(0), region));
			}
		};
	}

	public static DropdownElement copy(final IShape shape)
	{
		return new DropdownElement(new TextComponentString("Copy"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				final ItemStack item = new ItemStack(ItemsOrbis.block_chunk);

				final Minecraft mc = Minecraft.getMinecraft();

				NetworkingOrbis.sendPacketToServer(new PacketSetBlockDataContainerInHand(item, shape));
				mc.player.inventory.setInventorySlotContents(mc.player.inventory.currentItem, item);
			}
		};
	}

	public static DropdownElement fillWithVoid(final IShape shape)
	{
		return new DropdownElement(new TextComponentString("Fill With Void"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				final BlockFilter filter = new BlockFilter(BlockFilterHelper.getNewVoidLayer());

				NetworkingOrbis.sendPacketToServer(new PacketFilterShape(shape, filter));
			}
		};
	}

	public static DropdownElement delete(final IShape shape)
	{
		return new DropdownElement(new TextComponentString("Delete"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				// TODO: DESIGN DECISION: It deletes according to your current main hand. Might be confusing.
				final BlockFilter filter = new BlockFilter(BlockFilterHelper.getNewDeleteLayer(player.getHeldItemMainhand()));

				NetworkingOrbis.sendPacketToServer(new PacketFilterShape(shape, filter));
			}
		};
	}

	public static DropdownElement close()
	{
		return new DropdownElement(new TextComponentString("Close"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
			}
		};
	}

}
