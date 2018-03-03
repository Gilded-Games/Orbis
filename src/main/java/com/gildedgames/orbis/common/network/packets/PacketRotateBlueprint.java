package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRotateBlueprint implements IMessage
{

	public PacketRotateBlueprint()
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

	public static class HandlerServer extends MessageHandlerServer<PacketRotateBlueprint, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRotateBlueprint message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			playerOrbis.powers().getBlueprintPower()
					.setPlacingRotation(RotationHelp.getNextRotation(playerOrbis.powers().getBlueprintPower().getPlacingRotation(), true));

			return null;
		}
	}
}
