package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketChangeSelectionInput implements IMessage
{
	private int selectionInputIndex;

	public PacketChangeSelectionInput()
	{

	}

	public PacketChangeSelectionInput(final PlayerOrbis playerOrbis, final ISelectionInput selectionInput)
	{
		this.selectionInputIndex = playerOrbis.selectionInputs().getSelectionInputIndex(selectionInput);
	}

	public PacketChangeSelectionInput(final int powerIndex)
	{
		this.selectionInputIndex = powerIndex;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.selectionInputIndex = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.selectionInputIndex);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketChangeSelectionInput, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionInput message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionInputs().setCurrentSelectionInput(message.selectionInputIndex);
			}

			return null;
		}
	}

	public static class HandlerClient extends MessageHandlerClient<PacketChangeSelectionInput, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionInput message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionInputs().setCurrentSelectionInput(message.selectionInputIndex);
			}

			return null;
		}
	}
}
