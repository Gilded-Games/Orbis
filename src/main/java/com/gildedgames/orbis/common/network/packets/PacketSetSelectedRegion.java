package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerClient;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetSelectedRegion implements IMessage
{
	private int regionId;

	public PacketSetSelectedRegion()
	{

	}

	public PacketSetSelectedRegion(final int regionId)
	{
		this.regionId = regionId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.regionId = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.regionId);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetSelectedRegion, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetSelectedRegion message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final WorldShape shape = WorldObjectManager.get(player.world).getObject(message.regionId);

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			playerOrbis.powers().getSelectPower().setSelectedRegion(shape);

			return null;
		}
	}
}
