package com.gildedgames.orbis.api.packets.instances;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.Map;

public interface INetworkOrbis
{
	Map<Integer, ArrayList<byte[]>> getPacketParts();

	void sendPacketToDimension(final IMessage message, final int dimension);

	void sendPacketToAllPlayers(final IMessage message);

	void sendPacketToAllPlayersExcept(final IMessage message, final EntityPlayerMP player);

	void sendPacketToPlayer(final IMessage message, final EntityPlayerMP player);

	void sendPacketToWatching(final IMessage message, final EntityLivingBase entity, final boolean includeSelf);

	void sendPacketToServer(final IMessage message);
}
