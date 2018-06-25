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

public class PacketSetScheduleLayerInfo extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, layerIndex;

	private String displayName;

	private float edgeNoise;

	private boolean choosesPerBlock;

	public PacketSetScheduleLayerInfo()
	{

	}

	private PacketSetScheduleLayerInfo(final byte[] data)
	{
		super(data);
	}

	public PacketSetScheduleLayerInfo(final Blueprint blueprint, INode<IScheduleLayer, LayerLink> layer, String displayName, float edgeNoise,
			boolean choosesPerBlock)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);

		this.displayName = displayName;
		this.edgeNoise = edgeNoise;
		this.choosesPerBlock = choosesPerBlock;

		this.layerIndex = blueprint.getData().getScheduleLayerTree().get(layer);
	}

	public PacketSetScheduleLayerInfo(int worldObjectId, final int layerIndex, final String displayName, float edgeNoise, boolean choosesPerBlock)
	{
		this.worldObjectId = worldObjectId;

		this.displayName = displayName;
		this.edgeNoise = edgeNoise;
		this.choosesPerBlock = choosesPerBlock;

		this.layerIndex = layerIndex;
	}

	public PacketSetScheduleLayerInfo(final IDataIdentifier id, final int layerIndex, final String displayName, float edgeNoise, boolean choosesPerBlock)
	{
		this.id = id;

		this.displayName = displayName;
		this.edgeNoise = edgeNoise;
		this.choosesPerBlock = choosesPerBlock;

		this.layerIndex = layerIndex;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.layerIndex = tag.getInteger("layerIndex");

		this.displayName = tag.getString("displayName");
		this.edgeNoise = tag.getFloat("edgeNoise");
		this.choosesPerBlock = tag.getBoolean("choosesPerBlock");
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
		tag.setFloat("edgeNoise", this.edgeNoise);
		tag.setBoolean("choosesPerBlock", this.choosesPerBlock);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSetScheduleLayerInfo(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetScheduleLayerInfo, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduleLayerInfo message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IData data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					INode<IScheduleLayer, LayerLink> node = bData.getScheduleLayerTree().get(message.layerIndex);

					if (node != null)
					{
						IScheduleLayer layer = node.getData();

						layer.getOptions().getDisplayNameVar().setData(message.displayName);
						layer.getOptions().getEdgeNoiseVar().setData(message.edgeNoise);
						layer.getOptions().getChoosesPerBlockVar().setData(message.choosesPerBlock);
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

	public static class HandlerServer extends MessageHandlerServer<PacketSetScheduleLayerInfo, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduleLayerInfo message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IData data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					INode<IScheduleLayer, LayerLink> node = bData.getScheduleLayerTree().get(message.layerIndex);

					if (node != null)
					{
						IScheduleLayer layer = node.getData();

						layer.getOptions().getDisplayNameVar().setData(message.displayName);
						layer.getOptions().getEdgeNoiseVar().setData(message.edgeNoise);
						layer.getOptions().getChoosesPerBlockVar().setData(message.choosesPerBlock);
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
									new PacketSetScheduleLayerInfo(message.worldObjectId, message.layerIndex, message.displayName, message.edgeNoise,
											message.choosesPerBlock));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(
									new PacketSetScheduleLayerInfo(message.id, message.layerIndex, message.displayName, message.edgeNoise,
											message.choosesPerBlock));
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
