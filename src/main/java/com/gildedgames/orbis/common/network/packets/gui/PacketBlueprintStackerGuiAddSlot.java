package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.util.mc.SlotHashed;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintStackerGuiAddSlot implements IMessage
{

	private int slotId, posX, posY;

	public PacketBlueprintStackerGuiAddSlot()
	{

	}

	public PacketBlueprintStackerGuiAddSlot(int slotId, int posX, int posY)
	{
		this.slotId = slotId;
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.slotId = buf.readInt();
		this.posX = buf.readInt();
		this.posY = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.slotId);
		buf.writeInt(this.posX);
		buf.writeInt(this.posY);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintStackerGuiAddSlot, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintStackerGuiAddSlot message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerLoadData)
			{
				ContainerLoadData container = (ContainerLoadData) player.openContainer;

				container.stackerInventory.expand(message.slotId + 40 + 1);

				SlotHashed slot = new SlotHashed(container.stackerInventory, message.slotId + 40, message.posX, message.posY);

				container.getNavigator().put(slot, message.slotId, true);
				container.addStackerSlot(slot);
			}

			return null;
		}
	}
}
