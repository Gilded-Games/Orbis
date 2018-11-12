package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketRequestProject extends PacketMultipleParts
{

	private IProjectIdentifier project;

	public PacketRequestProject()
	{

	}

	private PacketRequestProject(final byte[] data)
	{
		super(data);
	}

	public PacketRequestProject(final IProjectIdentifier project)
	{
		this.project = project;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.project = funnel.get("project");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("project", this.project);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketRequestProject(data);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRequestProject, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRequestProject message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IProject> project = OrbisCore.getProjectManager().findProject(message.project);

			if (project.isPresent())
			{
				OrbisCore.network().sendPacketToPlayer(new PacketSendProjectCache(project.get()), (EntityPlayerMP) player);
			}
			else
			{
				OrbisCore.LOGGER.error("Could not find project requested", message.project);
			}

			return null;
		}
	}
}
