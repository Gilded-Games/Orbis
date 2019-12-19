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
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionFilter;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionGenerateGhostBlockDataContainer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis.lib.block.BlockFilter;
import com.gildedgames.orbis.lib.block.BlockFilterHelper;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.lib.data.pathway.Entrance;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.data.schedules.ISchedule;
import com.gildedgames.orbis.lib.world.IWorldObject;
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
				OrbisCore.network().sendPacketToServer(new PacketRemoveEntrance(blueprint));
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

				playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL).apply(player.getEntityWorld(), new WorldActionFilter(shape, filter, false));
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
				final BlockFilter filter = new BlockFilter(new BlockFilterHelper.BlockDeleteFilter());

				OrbisCore.network().sendPacketToServer(new PacketFilterShape(shape, filter));
			}
		};
	}

	public static DropdownElement generate(GhostBlockDataContainer ghost)
	{
		return new DropdownElement(new TextComponentString("Generate"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				PlayerOrbis.get(player).getWorldActionLog(WorldActionLogs.NORMAL).apply(player.world, new WorldActionGenerateGhostBlockDataContainer(ghost));
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
