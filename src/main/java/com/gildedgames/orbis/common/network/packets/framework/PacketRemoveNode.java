package com.gildedgames.orbis.common.network.packets.framework;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerClient;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.api.packets.util.PacketMultipleParts;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Framework;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRemoveNode extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, nodeId;

	public PacketRemoveNode()
	{

	}

	private PacketRemoveNode(final byte[] data)
	{
		super(data);
	}

	public PacketRemoveNode(IDataIdentifier id, int nodeId)
	{
		this.id = id;
		this.nodeId = nodeId;
	}

	public PacketRemoveNode(Framework framework, IFrameworkNode node)
	{
		this.worldObjectId = WorldObjectManager.get(framework.getWorld()).getID(framework);
		this.nodeId = framework.getData().getNodeId(node);
	}

	public PacketRemoveNode(int worldObjectId, int nodeId)
	{
		this.worldObjectId = worldObjectId;
		this.nodeId = nodeId;
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketRemoveNode(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("w");
		this.id = funnel.get("i");
		this.nodeId = tag.getInteger("ni");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("w", this.worldObjectId);
		funnel.set("i", this.id);
		tag.setInteger("ni", this.nodeId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketRemoveNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveNode message, final EntityPlayer player)
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

				if (data instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data;

					fData.removeNode(message.nodeId);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRemoveNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveNode message, final EntityPlayer player)
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

				if (data instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data;

					boolean removed = fData.removeNode(message.nodeId);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (removed && player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisAPI.network().sendPacketToAllPlayers(new PacketRemoveNode(message.worldObjectId, message.nodeId));
						}
						else
						{
							OrbisAPI.network().sendPacketToAllPlayers(new PacketRemoveNode(message.id, message.nodeId));
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
