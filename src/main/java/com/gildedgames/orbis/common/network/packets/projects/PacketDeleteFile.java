package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.client.gui.GuiLoadData;
import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class PacketDeleteFile extends PacketMultipleParts
{

	private IProjectIdentifier project;

	private String location;

	public PacketDeleteFile()
	{

	}

	private PacketDeleteFile(final byte[] data)
	{
		super(data);
	}

	public PacketDeleteFile(final IProjectIdentifier project, final String location)
	{
		this.project = project;
		this.location = location;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.project = funnel.get("project");
		this.location = tag.getString("location");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("project", this.project);
		tag.setString("location", this.location);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketDeleteFile(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketDeleteFile, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketDeleteFile message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IProject> project = OrbisCore.getProjectManager().findProject(message.project);

			if (project.isPresent())
			{
				Optional<UUID> id = project.get().getCache().getDataId(message.location);

				if (id.isPresent())
				{
					project.get().getCache().removeData(id.get());

					final File file = new File(project.get().getLocationAsFile(), message.location);

					if (file.delete())
					{
						if (Minecraft.getMinecraft().currentScreen instanceof GuiSaveData)
						{
							final GuiSaveData viewProjects = (GuiSaveData) Minecraft.getMinecraft().currentScreen;

							viewProjects.refreshNavigator();
						}

						if (Minecraft.getMinecraft().currentScreen instanceof GuiLoadData)
						{
							final GuiLoadData loadBlueprints = (GuiLoadData) Minecraft.getMinecraft().currentScreen;

							loadBlueprints.refreshNavigator();
						}
					}
				}
				else
				{
					OrbisCore.LOGGER.error("Could not find UUID for data location", message.project, this.getClass());
				}
			}
			else
			{
				OrbisCore.LOGGER.error("Could not find project", message.project, this.getClass());
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketDeleteFile, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketDeleteFile message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IProject> project = OrbisCore.getProjectManager().findProject(message.project);

			if (project.isPresent())
			{
				Optional<UUID> id = project.get().getCache().getDataId(message.location);

				if (id.isPresent())
				{
					project.get().getCache().removeData(id.get());

					final File file = new File(project.get().getLocationAsFile(), message.location);

					if (file.delete())
					{
						OrbisCore.network().sendPacketToPlayer(new PacketDeleteFile(message.project, message.location), (EntityPlayerMP) player);
					}
				}
			}

			return null;
		}
	}
}
