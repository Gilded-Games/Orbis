package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerEditBlueprintPostGen;
import com.gildedgames.orbis.common.containers.SlotGroup;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketPostGenRemoveLayer implements IMessage
{

	private int nodeId;

	public PacketPostGenRemoveLayer()
	{

	}

	public PacketPostGenRemoveLayer(int nodeId)
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

	public static class HandlerServer extends MessageHandlerServer<PacketPostGenRemoveLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketPostGenRemoveLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerEditBlueprintPostGen)
			{
				ContainerEditBlueprintPostGen container = (ContainerEditBlueprintPostGen) player.openContainer;

				container.stackerInventory.remove((message.nodeId * 2) + 37);
				container.stackerInventory.remove((message.nodeId * 2) + 1 + 37);

				SlotGroup layer = container.getNavigator().getNodes().get(message.nodeId);

				container.getNavigator().remove(message.nodeId);
				container.removeLayer(layer);
			}

			return null;
		}
	}
}
