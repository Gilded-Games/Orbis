package com.gildedgames.orbis.api.packets.util;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public interface IMessageMultipleParts
{
	byte[] getPartData();

	void clearPartData();

	IMessage[] getParts();

	void read(ByteBuf buf);

	void write(ByteBuf buf);
}
