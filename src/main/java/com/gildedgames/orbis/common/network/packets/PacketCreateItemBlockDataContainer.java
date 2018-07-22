package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.core.CreationData;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis_api.processing.DataPrimer;
import com.gildedgames.orbis_api.util.RotationHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCreateItemBlockDataContainer implements IMessage
{

	private BlockPos pos;

	private ItemStack stack;

	private NBTFunnel funnel;

	public PacketCreateItemBlockDataContainer()
	{

	}

	public PacketCreateItemBlockDataContainer(final ItemStack stack, final BlockPos pos)
	{
		this.stack = stack;
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.funnel = new NBTFunnel(NetworkUtils.readTagLimitless(buf));
		this.stack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("p", this.pos);

		ByteBufUtils.writeTag(buf, tag);
		ByteBufUtils.writeItemStack(buf, this.stack);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketCreateItemBlockDataContainer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketCreateItemBlockDataContainer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			message.pos = message.funnel.getPos("p");

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				final BlockDataContainer container = ItemBlockDataContainer.getDataContainer(message.stack);

				final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();

				final IRegion region = RotationHelp.regionFromCenter(message.pos, container, rotation);

				final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(player.world));
				primer.create(region, container, new CreationData(player.world, player).pos(region.getMin()).rotation(rotation).placesAir(false), null);
			}

			return null;
		}
	}
}
