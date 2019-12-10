package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstance;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.mc.BlockPosDimension;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketTeleportOrbis implements IMessage
{

	public PacketTeleportOrbis()
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

	public static class HandlerServer extends MessageHandlerServer<PacketTeleportOrbis, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketTeleportOrbis message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (!playerOrbis.inDeveloperMode())
			{
				return null;
			}

			final OrbisInstanceHandler handler = OrbisCore.ORBIS_INSTANCE_HANDLER;

			World world = player.getEntityWorld();
			BlockPos pos = player.getPosition();

			final OrbisInstance instance = handler.get(playerOrbis, new BlockPosDimension(pos, world.provider.getDimension()));

			if (player.dimension == instance.getDimensionId())
			{
				instance.setInsideEntrance(new BlockPosDimension(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(),
						instance.getDimensionId()));

				handler.teleportBack((EntityPlayerMP) player);
			}
			else
			{
				handler.teleportToInst((EntityPlayerMP) player, instance);
			}

			return null;
		}
	}
}
