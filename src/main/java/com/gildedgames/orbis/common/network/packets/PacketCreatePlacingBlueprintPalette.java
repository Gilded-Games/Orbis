package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
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
				final BlueprintData data = playerOrbis.powers().getBlueprintPower().getPlacingPalette().fetchRandom(player.world, player.world.rand);

				final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();

				final IRegion region = RotationHelp.regionFromCenter(message.pos, data, rotation);

				final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(player.world));

				primer.create(data.getBlockDataContainer(), new CreationData(player.world, player).pos(region.getMin()).rotation(rotation).placesAir(false));
			}

			return null;
		}
	}
}
