package com.gildedgames.orbis.client.gui.util.directory.nodes;

import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNodeFactory;
import com.gildedgames.orbis_api.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.json.JsonData;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.impl.OrbisProjectManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class OrbisNavigatorNodeFactory implements IDirectoryNodeFactory
{
	private Function<String, Boolean> extensionValidator;

	public OrbisNavigatorNodeFactory()
	{

	}

	public OrbisNavigatorNodeFactory(Function<String, Boolean> extensionValidator)
	{
		this.extensionValidator = extensionValidator;
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
			OrbisAPI.LOGGER.error(e);
		}

		if (file.isDirectory())
		{
			if (OrbisProjectManager.isProjectDirectory(file))
			{
				OrbisAPI.services().getProjectManager().refreshCache();

				try
				{
					final IProject project = OrbisAPI.services().getProjectManager().findProject(file.getName());

					if (project != null)
					{
						// TODO: Refresh cache in case contents of project changed outside of game.
						//project.loadAndCacheData();

						node = new NavigatorNodeProject(file, project);
					}
				}
				catch (final OrbisMissingProjectException e)
				{
					OrbisAPI.LOGGER.error("Project couldn't be found in cache, skipping node!", e);
				}
			}
			else
			{
				node = new NavigatorNodeFolder(file);
			}
		}
		else
		{
			if (this.extensionValidator != null && !this.extensionValidator.apply(extension))
			{
				return null;
			}

			switch (extension)
			{
				case BlueprintData.EXTENSION:
					node = new NavigatorNodeBlueprint(file);
					break;
				case FrameworkData.EXTENSION:
					node = new NavigatorNodeFramework(file);
					break;
				case BlueprintStackerData.EXTENSION:
					node = new NavigatorNodeBlueprintStacker(file);
					break;
				case JsonData.EXTENSION:
					node = new NavigatorNodeJson(file);
					break;
				default:
					node = new NavigatorNodeFile(file);
			}
		}

		return node;
	}
}
