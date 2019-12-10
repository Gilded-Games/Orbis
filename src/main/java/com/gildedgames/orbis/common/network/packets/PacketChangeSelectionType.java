package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_types.ISelectionType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketChangeSelectionType implements IMessage
{
	private int selectionTypeIndex;

	public PacketChangeSelectionType()
	{

	}

	public PacketChangeSelectionType(final PlayerOrbis playerOrbis, final ISelectionType selectionType)
	{
		this.selectionTypeIndex = playerOrbis.selectionTypes().getSelectionTypeIndex(selectionType.getClass());
	}

	public PacketChangeSelectionType(final int powerIndex)
	{
		this.selectionTypeIndex = powerIndex;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.selectionTypeIndex = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.selectionTypeIndex);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketChangeSelectionType, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionType message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionTypes().setCurrentSelectionType(message.selectionTypeIndex);
			}

			return null;
		}
	}

	public static class HandlerClient extends MessageHandlerClient<PacketChangeSelectionType, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionType message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionTypes().setCurrentSelectionType(message.selectionTypeIndex);
			}

			return null;
		}
	}
}
