package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketDeveloperMode implements IMessage
{

	private boolean flag = true;

	public PacketDeveloperMode()
	{

	}

	public PacketDeveloperMode(final boolean flag)
	{
		this.flag = flag;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.flag = buf.readBoolean();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeBoolean(this.flag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketDeveloperMode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketDeveloperMode message, final EntityPlayer player)
		{
			if (player == null || player.world == null || (player instanceof EntityPlayerMP && !player.getServer().getPlayerList()
					.canSendCommands(player.getGameProfile())))
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			playerOrbis.setDeveloperMode(message.flag);

			return null;
		}
	}
}
