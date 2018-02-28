package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IProject;
import com.gildedgames.orbis.api.data.management.IProjectCache;
import com.gildedgames.orbis.api.data.management.IProjectIdentifier;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.client.gui.blueprint.GuiSaveBlueprint;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.MessageHandlerClient;
import com.gildedgames.orbis.common.network.util.PacketMultipleParts;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class PacketSendProject extends PacketMultipleParts
{

	private IProjectIdentifier project;

	private IProjectCache cache;

	private LocalDateTime lastChanged;

	public PacketSendProject()
	{

	}

	private PacketSendProject(final byte[] data)
	{
		super(data);
	}

	public PacketSendProject(final IProject project)
	{
		this.project = project.getProjectIdentifier();
		this.cache = project.getCache();
		this.lastChanged = project.getMetadata().getLastChanged();
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.project = funnel.get("project");
		this.cache = funnel.get("cache");
		this.lastChanged = funnel.getDate("lastChanged");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("project", this.project);
		funnel.set("cache", this.cache);
		funnel.setDate("lastChanged", this.lastChanged);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSendProject(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSendProject, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSendProject message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IProject project = OrbisCore.getProjectManager().findProject(message.project);

				if (project != null)
				{
					project.setCache(message.cache);

					/**
					 * Save all state to disk.
					 */
					for (final IData data : message.cache.getAllData())
					{
						final File file = new File(project.getLocationAsFile(), message.cache.getDataLocation(data.getMetadata().getIdentifier().getDataId()));

						try (FileOutputStream out = new FileOutputStream(file))
						{
							final NBTTagCompound tag = new NBTTagCompound();
							final NBTFunnel funnel = new NBTFunnel(tag);

							funnel.set("state", data);

							CompressedStreamTools.writeCompressed(tag, out);
						}
						catch (final IOException e)
						{
							OrbisCore.LOGGER.error("Failed to save project state to disk", e);
						}
					}

					project.getMetadata().setDownloaded(true);
					project.getMetadata().setDownloading(false);

					project.getMetadata().setLastChanged(message.lastChanged);
				}

				OrbisCore.LOGGER.debug("Project downloaded! " + project.getLocationAsFile().getName());
			}
			catch (final OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			if (Minecraft.getMinecraft().currentScreen instanceof GuiSaveBlueprint)
			{
				final GuiSaveBlueprint viewProjects = (GuiSaveBlueprint) Minecraft.getMinecraft().currentScreen;

				viewProjects.refreshNavigator();
			}

			return null;
		}
	}
}
