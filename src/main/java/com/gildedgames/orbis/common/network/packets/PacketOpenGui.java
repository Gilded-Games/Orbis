package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketOpenGui implements IMessage
{

	private int modGuiId, x, y, z;

	public PacketOpenGui()
	{

	}

	public PacketOpenGui(int modGuiId, int x, int y, int z)
	{
		this.modGuiId = modGuiId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.modGuiId = buf.readInt();
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.modGuiId);
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketOpenGui, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketOpenGui message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			player.openGui(OrbisCore.INSTANCE, message.modGuiId, player.world, message.x, message.y, message.z);

			return null;
		}
	}
}
