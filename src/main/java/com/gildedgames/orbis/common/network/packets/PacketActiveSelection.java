package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nullable;

public class PacketActiveSelection implements IMessage
{
	private BlockPos start, end;

	private IShape shape;

	private NBTFunnel funnel;

	public PacketActiveSelection()
	{

	}

	public PacketActiveSelection(final IShape shape, @Nullable BlockPos start, @Nullable BlockPos end)
	{
		this.shape = shape;
		this.start = start;
		this.end = end;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);

		this.funnel = new NBTFunnel(tag);
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("shape", this.shape);
		funnel.setPos("s", this.start);
		funnel.setPos("e", this.end);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketActiveSelection, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketActiveSelection message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IShape shape = message.funnel.get(player.world, "shape");

			BlockPos start = message.funnel.getPos("s");
			BlockPos end = message.funnel.getPos("e");

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			selectionInput.setActiveSelection(new WorldShape(shape, player.getEntityWorld()));

			IShapeSelector selector = playerOrbis.powers().getCurrentPower().getShapeSelector();

			final ItemStack held = player.getHeldItemMainhand();

			if (held.getItem() instanceof IShapeSelector)
			{
				selector = (IShapeSelector) held.getItem();
			}

			selector.onSelect(playerOrbis, shape, player.world, start, end);

			return null;
		}
	}
}
