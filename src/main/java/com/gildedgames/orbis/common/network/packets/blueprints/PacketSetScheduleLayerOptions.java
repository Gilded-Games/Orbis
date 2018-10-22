package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.LayerLink;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketSetScheduleLayerOptions extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, layerIndex;

	private String displayName;

	private boolean replacesSolidBlocks;

	public PacketSetScheduleLayerOptions()
	{

	}

	private PacketSetScheduleLayerOptions(final byte[] data)
	{
		super(data);
	}

	public PacketSetScheduleLayerOptions(final Blueprint blueprint, INode<IScheduleLayer, LayerLink> layer, String displayName,
			boolean replacesSolidBlocks)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);

		this.displayName = displayName;
		this.replacesSolidBlocks = replacesSolidBlocks;

		this.layerIndex = blueprint.getData().getScheduleLayerTree().get(layer);
	}

	public PacketSetScheduleLayerOptions(int worldObjectId, final int layerIndex, final String displayName, boolean replacesSolidBlocks)
	{
		this.worldObjectId = worldObjectId;

		this.displayName = displayName;
		this.replacesSolidBlocks = replacesSolidBlocks;

		this.layerIndex = layerIndex;
	}

	public PacketSetScheduleLayerOptions(final IDataIdentifier id, final int layerIndex, final String displayName, boolean replacesSolidBlocks)
	{
		this.id = id;

		this.displayName = displayName;
		this.replacesSolidBlocks = replacesSolidBlocks;

		this.layerIndex = layerIndex;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.layerIndex = tag.getInteger("layerIndex");

		this.displayName = tag.getString("displayName");
		this.replacesSolidBlocks = tag.getBoolean("replacesSolidBlocks");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("layerIndex", this.layerIndex);

		tag.setString("displayName", this.displayName);
		tag.setBoolean("replacesSolidBlocks", this.replacesSolidBlocks);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSetScheduleLayerOptions(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetScheduleLayerOptions, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduleLayerOptions message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final Optional<IData> data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = Optional.of(worldObject.getData());
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data.isPresent() && data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					INode<IScheduleLayer, LayerLink> node = bData.getScheduleLayerTree().get(message.layerIndex);

					if (node != null)
					{
						IScheduleLayer layer = node.getData();

						layer.getOptions().getDisplayNameVar().setData(message.displayName);
						layer.getOptions().getReplacesSolidBlocksVar().setData(message.replacesSolidBlocks);
					}
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetScheduleLayerOptions, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduleLayerOptions message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final Optional<IData> data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = Optional.of(worldObject.getData());
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data.isPresent() && data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					INode<IScheduleLayer, LayerLink> node = bData.getScheduleLayerTree().get(message.layerIndex);

					if (node != null)
					{
						IScheduleLayer layer = node.getData();

						layer.getOptions().getDisplayNameVar().setData(message.displayName);
						layer.getOptions().getReplacesSolidBlocksVar().setData(message.replacesSolidBlocks);
					}

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(
									new PacketSetScheduleLayerOptions(message.worldObjectId, message.layerIndex, message.displayName,
											message.replacesSolidBlocks));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(
									new PacketSetScheduleLayerOptions(message.id, message.layerIndex, message.displayName,
											message.replacesSolidBlocks));
						}
					}
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}
}
