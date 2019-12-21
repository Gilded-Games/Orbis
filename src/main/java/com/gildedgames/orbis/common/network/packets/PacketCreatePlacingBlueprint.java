package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.core.ICreationData;
import com.gildedgames.orbis.lib.core.baking.BakedBlueprint;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.RotationHelp;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCreatePlacingBlueprint implements IMessage
{

	private BlockPos pos;

	private NBTFunnel funnel;

	public PacketCreatePlacingBlueprint()
	{

	}

	public PacketCreatePlacingBlueprint(final BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.funnel = new NBTFunnel(NetworkUtils.readTagLimitless(buf));
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("p", this.pos);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketCreatePlacingBlueprint, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketCreatePlacingBlueprint message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			message.pos = message.funnel.getPos("p");

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				final BlueprintData data = playerOrbis.powers().getBlueprintPower().getPlacingBlueprint();

				final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();

				final IRegion region = RotationHelp.regionFromCenter(message.pos, data, rotation);

				final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(player.world));

				ICreationData creationData = new CreationData(player.world, player).pos(region.getMin()).rotation(rotation).placesAir(false);
				BakedBlueprint baked = new BakedBlueprint(data, creationData);

				primer.place(baked, creationData.getPos());
			}

			return null;
		}
	}
}
