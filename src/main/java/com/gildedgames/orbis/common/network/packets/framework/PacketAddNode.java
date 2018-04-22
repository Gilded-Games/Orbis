package com.gildedgames.orbis.common.network.packets.framework;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.FrameworkNode;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
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

public class PacketAddNode extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId;

	private FrameworkNode node;

	public PacketAddNode()
	{

	}

	private PacketAddNode(final byte[] data)
	{
		super(data);
	}

	public PacketAddNode(IDataIdentifier id, FrameworkNode node)
	{
		this.id = id;
		this.node = node;
	}

	public PacketAddNode(Framework framework, FrameworkNode node)
	{
		this.worldObjectId = WorldObjectManager.get(framework.getWorld()).getID(framework);
		this.node = node;
	}

	public PacketAddNode(int worldObjectId, FrameworkNode node)
	{
		this.worldObjectId = worldObjectId;
		this.node = node;
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketAddNode(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("w");
		this.id = funnel.get("i");
		this.node = funnel.get("n");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("w", this.worldObjectId);
		funnel.set("i", this.id);
		funnel.set("n", this.node);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketAddNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddNode message, final EntityPlayer player)
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

					fData.addNode(message.node);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketAddNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddNode message, final EntityPlayer player)
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

					fData.addNode(message.node);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketAddNode(message.worldObjectId, message.node));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketAddNode(message.id, message.node));
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
