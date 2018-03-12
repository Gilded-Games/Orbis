package com.gildedgames.orbis.client.gui.util.directory.nodes;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.management.IProject;
import com.gildedgames.orbis.api.data.management.impl.OrbisProjectManager;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNodeFactory;
import com.gildedgames.orbis.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis.common.OrbisCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OrbisNavigatorNodeFactory implements IDirectoryNodeFactory
{
	public static final String BLUEPRINT = "blueprint";

	public static final String FRAMEWORK = "framework";

	private String viewOnly;

	public OrbisNavigatorNodeFactory()
	{

	}

	public OrbisNavigatorNodeFactory(String viewOnly)
	{
		this.viewOnly = viewOnly;
	}

	@Override
	public INavigatorNode createFrom(final File file, final String extension)
	{
		INavigatorNode node = null;

		try
		{
			if (Files.getAttribute(Paths.get(file.getPath()), "dos:hidden") == Boolean.TRUE)
			{
				return null;
			}
		}
		catch (final IOException e)
		{
			OrbisCore.LOGGER.error(e);
		}

		if (file.isDirectory())
		{
			if (OrbisProjectManager.isProjectDirectory(file))
			{
				OrbisCore.getProjectManager().refreshCache();

				try
				{
					final IProject project = OrbisCore.getProjectManager().findProject(file.getName());

					if (project != null)
					{
						// TODO: Refresh cache in case contents of project changed outside of game.
						//project.loadAndCacheData();

						node = new NavigatorNodeProject(file, project);
					}
				}
				catch (final OrbisMissingProjectException e)
				{
					OrbisCore.LOGGER.error("Project couldn't be found in cache, skipping node!", e);
				}
			}
			else
			{
				node = new NavigatorNodeFolder(file);
			}
		}
		else
		{
			if (this.viewOnly != null && !extension.equals(this.viewOnly))
			{
				return null;
			}

			switch (extension)
			{
				case BLUEPRINT:
					node = new NavigatorNodeBlueprint(file);
					break;
				case FRAMEWORK:
					node = new NavigatorNodeFramework(file);
					break;
				default:
					node = new NavigatorNodeFile(file);
			}
		}

		return node;
	}
}
