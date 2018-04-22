package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.client.gui.GuiLoadData;
import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
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
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
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

			try
			{
				final IProject project = OrbisCore.getProjectManager().findProject(message.project);

				project.getCache().removeData(project.getCache().getDataId(message.location));

				final File file = new File(project.getLocationAsFile(), message.location);

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
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
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

			final IProject project = OrbisCore.getProjectManager().findProject(message.project);

			project.getCache().removeData(project.getCache().getDataId(message.location));

			final File file = new File(project.getLocationAsFile(), message.location);

			if (file.delete())
			{
				OrbisCore.network().sendPacketToPlayer(new PacketDeleteFile(message.project, message.location), (EntityPlayerMP) player);
			}

			return null;
		}
	}
}
