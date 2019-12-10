package com.gildedgames.orbis.common.network.packets.world_actions;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketUndoWorldAction implements IMessage
{
	private String worldActionLogId;

	public PacketUndoWorldAction()
	{

	}

	public PacketUndoWorldAction(String worldActionLogId)
	{
		this.worldActionLogId = worldActionLogId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.worldActionLogId = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, this.worldActionLogId);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketUndoWorldAction, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketUndoWorldAction message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getWorldActionLog(message.worldActionLogId).undo(player.getEntityWorld());
			}

			return null;
		}
	}
}
