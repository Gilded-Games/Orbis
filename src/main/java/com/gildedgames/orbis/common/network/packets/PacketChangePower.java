package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketChangePower implements IMessage
{
	private int powerIndex;

	public PacketChangePower()
	{

	}

	public PacketChangePower(final PlayerOrbis playerOrbis, final IGodPower power)
	{
		this.powerIndex = playerOrbis.powers().getPowerIndex(power.getClass());
	}

	public PacketChangePower(final int powerIndex)
	{
		this.powerIndex = powerIndex;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.powerIndex = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.powerIndex);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketChangePower, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangePower message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.powers().setCurrentPower(message.powerIndex);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketChangePower, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangePower message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.powers().setCurrentPower(message.powerIndex);
			}

			return null;
		}
	}
}
