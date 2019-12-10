package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.mc.SlotHashed;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintStackerGuiRemoveSlot implements IMessage
{

	private int nodeId, slotNumber;

	public PacketBlueprintStackerGuiRemoveSlot()
	{

	}

	public PacketBlueprintStackerGuiRemoveSlot(int nodeId)
	{
		this.nodeId = nodeId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.nodeId = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.nodeId);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintStackerGuiRemoveSlot, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintStackerGuiRemoveSlot message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerLoadData)
			{
				ContainerLoadData container = (ContainerLoadData) player.openContainer;

				container.stackerInventory.remove(message.nodeId + 40);

				SlotHashed slot = container.getNavigator().getNodes().get(message.nodeId);

				container.getNavigator().remove(message.nodeId);
				container.removeStackerSlot(slot);
			}

			return null;
		}
	}
}
