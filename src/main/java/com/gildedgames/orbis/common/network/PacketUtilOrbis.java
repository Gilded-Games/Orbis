package com.gildedgames.orbis.common.network;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUtilOrbis
{
	public static <REQ extends IMessage, RES extends IMessage>
	void checkThread(final MessageHandlerServer<REQ, RES> handler, final REQ message, final MessageContext ctx)
	{
		final WorldServer world = ctx.getServerHandler().player.getServerWorld();

		if (!world.isCallingFromMinecraftThread())
		{
			world.addScheduledTask(() ->
			{
				handler.onMessage(message, ctx);
			});
		}
	}
}
