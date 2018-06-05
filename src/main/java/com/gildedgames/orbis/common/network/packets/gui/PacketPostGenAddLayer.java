package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerEditBlueprintPostGen;
import com.gildedgames.orbis.common.containers.SlotGroup;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.util.mc.SlotHashed;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.commons.lang3.tuple.Pair;

public class PacketPostGenAddLayer implements IMessage
{

	private int slotId, posX, posY, posX2, posY2;

	public PacketPostGenAddLayer()
	{

	}

	public PacketPostGenAddLayer(int slotId, int posX, int posY, int posX2, int posY2)
	{
		this.slotId = slotId;

		this.posX = posX;
		this.posY = posY;
		this.posX2 = posX2;
		this.posY2 = posY2;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.slotId = buf.readInt();
		this.posX = buf.readInt();
		this.posY = buf.readInt();
		this.posX2 = buf.readInt();
		this.posY2 = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.slotId);
		buf.writeInt(this.posX);
		buf.writeInt(this.posY);
		buf.writeInt(this.posX2);
		buf.writeInt(this.posY2);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketPostGenAddLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketPostGenAddLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerEditBlueprintPostGen)
			{
				ContainerEditBlueprintPostGen container = (ContainerEditBlueprintPostGen) player.openContainer;

				container.stackerInventory.expand((message.slotId * 2) + 37 + 2);

				SlotHashed slot = new SlotHashed(container.stackerInventory, (message.slotId * 2) + 37, message.posX, message.posY);
				SlotHashed slot2 = new SlotHashed(container.stackerInventory, (message.slotId * 2) + 37 + 1, message.posX2, message.posY2);

				SlotGroup layer = new SlotGroup(Pair.of(slot, slot2), message.slotId);

				container.getNavigator().put(layer, message.slotId, true);
				container.addLayer(layer);
			}

			return null;
		}
	}
}
