package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;

public class PacketActiveSelectionMultiple implements IMessage
{
	private List<BlockPos> multiplePositions;

	private IShape shape;

	private NBTFunnel funnel;

	public PacketActiveSelectionMultiple()
	{

	}

	public PacketActiveSelectionMultiple(final IShape shape, List<BlockPos> multiplePositions)
	{
		this.shape = shape;
		this.multiplePositions = multiplePositions;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);

		this.funnel = new NBTFunnel(tag);
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("shape", this.shape);
		funnel.setList("p", this.multiplePositions, NBTFunnel.POS_SETTER);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketActiveSelectionMultiple, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketActiveSelectionMultiple message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IShape shape = message.funnel.get(player.world, "shape");

			List<BlockPos> multiplePositions = message.funnel.getList("p", NBTFunnel.POS_GETTER);

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			selectionInput.setActiveSelection(new WorldShape(shape, player.getEntityWorld()));

			IShapeSelector selector = playerOrbis.powers().getCurrentPower().getShapeSelector();

			final ItemStack held = player.getHeldItemMainhand();

			if (held.getItem() instanceof IShapeSelector)
			{
				selector = (IShapeSelector) held.getItem();
			}

			selector.onSelectMultiple(playerOrbis, shape, player.world, multiplePositions);

			return null;
		}
	}
}
