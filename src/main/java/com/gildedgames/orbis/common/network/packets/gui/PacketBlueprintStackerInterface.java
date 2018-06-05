package com.gildedgames.orbis.common.network.packets.gui;

import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintStackerInterface implements IMessage
{

	private boolean start;

	public PacketBlueprintStackerInterface()
	{

	}

	public PacketBlueprintStackerInterface(boolean start)
	{
		this.start = start;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.start = buf.readBoolean();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeBoolean(this.start);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintStackerInterface, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintStackerInterface message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.openContainer instanceof ContainerLoadData)
			{
				ContainerLoadData container = (ContainerLoadData) player.openContainer;

				if (message.start)
				{
					container.startStackerInterface();
				}
				else
				{
					container.stopStackerInterface();
				}
			}

			return null;
		}
	}
}
