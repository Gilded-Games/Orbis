package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerProjectModule extends PlayerOrbisModule
{

	private IProject currentProject;

	public PlayerProjectModule(final PlayerOrbis playerOrbis)
	{
		super(playerOrbis);
	}

	public IProject getCurrentProject()
	{
		return this.currentProject;
	}

	public void setCurrentProject(final IProject project)
	{
		this.currentProject = project;
	}

	@Override
	public void onUpdate()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		if (this.currentProject != null)
		{
			funnel.set("projectId", this.currentProject.getProjectIdentifier());
		}
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		final IProjectIdentifier id = funnel.get("projectId");

		if (id != null)
		{
			try
			{
				OrbisCore.getProjectManager().findProject(id).ifPresent(project -> this.currentProject = project);
			}
			catch (final OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}
		}
	}
}
