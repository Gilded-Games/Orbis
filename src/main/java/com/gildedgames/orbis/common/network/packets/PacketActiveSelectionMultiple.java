package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Set;

public class PacketActiveSelectionMultiple implements IMessage
{
	private Set<BlockPos> multiplePositions;

	private IShape shape;

	private NBTFunnel funnel;

	public PacketActiveSelectionMultiple()
	{

	}

	public PacketActiveSelectionMultiple(final IShape shape, Set<BlockPos> multiplePositions)
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
		funnel.setSet("p", this.multiplePositions, NBTFunnel.POS_SETTER);

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

			Set<BlockPos> multiplePositions = message.funnel.getSet("p", NBTFunnel.POS_GETTER);

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
