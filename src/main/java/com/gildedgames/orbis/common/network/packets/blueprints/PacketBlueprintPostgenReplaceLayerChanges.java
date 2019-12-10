package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.data.schedules.PostGenReplaceLayer;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintPostgenReplaceLayerChanges extends PacketMultipleParts
{

	private int worldObjectId;

	private int layerId;

	private ItemStack required, replaced;

	public PacketBlueprintPostgenReplaceLayerChanges()
	{

	}

	private PacketBlueprintPostgenReplaceLayerChanges(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintPostgenReplaceLayerChanges(final Blueprint blueprint, final int scheduleLayerIndex, ItemStack required, ItemStack replaced)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.layerId = scheduleLayerIndex;
		this.required = required;
		this.replaced = replaced;
	}

	public PacketBlueprintPostgenReplaceLayerChanges(final int worldObjectId, final int scheduleLayerIndex, ItemStack required, ItemStack replaced)
	{
		this.worldObjectId = worldObjectId;
		this.layerId = scheduleLayerIndex;
		this.required = required;
		this.replaced = replaced;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.layerId = tag.getInteger("layerId");
		this.required = funnel.getStack("required");
		this.replaced = funnel.getStack("replaced");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("layerId", this.layerId);
		funnel.setStack("required", this.required);
		funnel.setStack("replaced", this.replaced);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintPostgenReplaceLayerChanges(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintPostgenReplaceLayerChanges, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintPostgenReplaceLayerChanges message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

			if (worldObject instanceof Blueprint)
			{
				final Blueprint b = (Blueprint) worldObject;

				PostGenReplaceLayer layer = b.getData().getPostGenReplaceLayer(message.layerId);

				layer.setRequired(message.required);
				layer.setReplaced(message.replaced);
			}
			else if (worldObject == null)
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in the world", message.worldObjectId, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("World object isn't a Blueprint", message.worldObjectId, this.getClass());
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintPostgenReplaceLayerChanges, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintPostgenReplaceLayerChanges message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

			if (worldObject instanceof Blueprint)
			{
				final Blueprint b = (Blueprint) worldObject;

				PostGenReplaceLayer layer = b.getData().getPostGenReplaceLayer(message.layerId);

				layer.setRequired(message.required);
				layer.setReplaced(message.replaced);

				if (player.world.getMinecraftServer().isDedicatedServer())
				{
					OrbisCore.network()
							.sendPacketToAllPlayers(new PacketBlueprintPostgenReplaceLayerChanges(message.worldObjectId, message.layerId, message.required,
									message.replaced));
				}
			}
			else if (worldObject == null)
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in the world", message.worldObjectId, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("World object isn't a Blueprint", message.worldObjectId, this.getClass());
			}

			return null;
		}
	}
}
