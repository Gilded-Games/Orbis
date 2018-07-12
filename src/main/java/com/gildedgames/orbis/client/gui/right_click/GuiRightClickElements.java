package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.packets.PacketFilterShape;
import com.gildedgames.orbis.common.network.packets.PacketSetBlockDataContainerInHand;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketRemoveEntrance;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketRemoveSchedule;
import com.gildedgames.orbis.common.network.packets.framework.PacketRemoveNode;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionFilter;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.block.BlockFilter;
import com.gildedgames.orbis_api.block.BlockFilterHelper;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.world.IWorldObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

public class GuiRightClickElements
{
	public static long lastCloseTime;

	public static DropdownElement remove(final IWorldObject region)
	{
		return new DropdownElement(new TextComponentString("Remove"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				OrbisCore.network().sendPacketToServer(new PacketWorldObjectRemove(region.getWorld(), region));
			}
		};
	}

	public static DropdownElement remove(Framework framework, IFrameworkNode node)
	{
		return new DropdownElement(new TextComponentString("Remove"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				OrbisCore.network().sendPacketToServer(new PacketRemoveNode(framework, node));
			}
		};
	}

	public static DropdownElement remove(Blueprint blueprint, ISchedule schedule)
	{
		return new DropdownElement(new TextComponentString("Remove"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				OrbisCore.network().sendPacketToServer(new PacketRemoveSchedule(blueprint, schedule));
			}
		};
	}

	public static DropdownElement remove(Blueprint blueprint, Entrance entrance)
	{
		return new DropdownElement(new TextComponentString("Remove"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				OrbisCore.network().sendPacketToServer(new PacketRemoveEntrance(blueprint, entrance));
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

				OrbisCore.network().sendPacketToServer(new PacketSetBlockDataContainerInHand(item, shape));
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
				PlayerOrbis playerOrbis = PlayerOrbis.get(player);
				final BlockFilter filter = new BlockFilter(BlockFilterHelper.getNewVoidLayer());

				playerOrbis.getWorldActionLog().track(player.getEntityWorld(), new WorldActionFilter(shape, filter, playerOrbis.powers().isScheduling()));
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

				OrbisCore.network().sendPacketToServer(new PacketFilterShape(shape, filter));
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
