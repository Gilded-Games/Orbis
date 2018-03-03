package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerClient;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketDeveloperReach implements IMessage
{

	private double extendedReach;

	public PacketDeveloperReach()
	{

	}

	public PacketDeveloperReach(final double extendedReach)
	{
		this.extendedReach = extendedReach;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.extendedReach = buf.readDouble();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeDouble(this.extendedReach);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketDeveloperReach, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketDeveloperReach message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			playerOrbis.setDeveloperReach(message.extendedReach);

			return null;
		}
	}

	public static class HandlerClient extends MessageHandlerClient<PacketDeveloperReach, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketDeveloperReach message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.setDeveloperReach(message.extendedReach);
			}

			return null;
		}
	}
}
