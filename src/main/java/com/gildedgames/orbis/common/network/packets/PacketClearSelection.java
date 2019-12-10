package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketClearSelection implements IMessage
{

	public PacketClearSelection()
	{

	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{

	}

	@Override
	public void toBytes(final ByteBuf buf)
	{

	}

	public static class HandlerServer extends MessageHandlerServer<PacketClearSelection, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketClearSelection message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerORbis = PlayerOrbis.get(player);

			if (playerORbis.inDeveloperMode())
			{
				playerORbis.selectionInputs().getCurrentSelectionInput().clearSelection();
			}

			return null;
		}
	}
}
