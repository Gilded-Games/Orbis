package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.mc.SlotHashed;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;

public class PacketBlueprintStackerGuiDisplaySlots implements IMessage
{

	private int[] slots;

	public PacketBlueprintStackerGuiDisplaySlots()
	{

	}

	public PacketBlueprintStackerGuiDisplaySlots(int[] slots)
	{
		this.slots = slots;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.slots = new int[buf.readInt()];

		for (int i = 0; i < this.slots.length; i++)
		{
			this.slots[i] = buf.readInt();
		}
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.slots.length);

		for (int i = 0; i < this.slots.length; i++)
		{
			buf.writeInt(this.slots[i]);
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintStackerGuiDisplaySlots, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintStackerGuiDisplaySlots message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerLoadData)
			{
				ContainerLoadData container = (ContainerLoadData) player.openContainer;

				List<SlotHashed> visible = Lists.newArrayList();

				for (int i = 0; i < message.slots.length; i++)
				{
					visible.add(container.getNavigator().getNodes().get(message.slots[i]));
				}

				container.display(visible);
			}

			return null;
		}
	}
}
