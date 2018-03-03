package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCreatePlacingBlueprintPalette implements IMessage
{

	private BlockPos pos;

	private NBTFunnel funnel;

	public PacketCreatePlacingBlueprintPalette()
	{

	}

	public PacketCreatePlacingBlueprintPalette(final BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.funnel = new NBTFunnel(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("p", this.pos);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketCreatePlacingBlueprintPalette, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketCreatePlacingBlueprintPalette message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			message.pos = message.funnel.getPos("p");

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(player.world));

				final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();
				ICreationData data = new CreationData(player.world, player).pos(message.pos).rotation(rotation).placesAir(false);

				primer.create(playerOrbis.powers().getBlueprintPower().getPlacingPalette(), data);
			}

			return null;
		}
	}
}
