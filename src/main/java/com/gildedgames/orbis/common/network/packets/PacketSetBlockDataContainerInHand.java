package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetBlockDataContainerInHand implements IMessage
{

	private ItemStack stack;

	private IShape shape;

	private NBTFunnel funnel;

	public PacketSetBlockDataContainerInHand()
	{

	}

	public PacketSetBlockDataContainerInHand(final ItemStack stack, final IShape shape)
	{
		this.stack = stack;
		this.shape = shape;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.stack = ByteBufUtils.readItemStack(buf);

		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		this.funnel = new NBTFunnel(tag);
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		ByteBufUtils.writeItemStack(buf, this.stack);

		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("shape", this.shape);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetBlockDataContainerInHand, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetBlockDataContainerInHand message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (player.isCreative())
			{
				message.shape = message.funnel.get(player.world, "shape");

				player.inventory.setInventorySlotContents(player.inventory.currentItem, message.stack);
				final BlockDataContainer container = BlockDataContainer.fromShape(player.world, message.shape);

				ItemBlockDataContainer.setDataContainer(player, message.stack, container);
			}

			return null;
		}
	}
}
