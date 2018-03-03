package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IProject;
import com.gildedgames.orbis.api.data.management.IProjectIdentifier;
import com.gildedgames.orbis.api.data.management.impl.ProjectIdentifier;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.client.gui.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.data.directory.DirectoryNavigator;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNavigatorListener;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNode;
import com.gildedgames.orbis.client.gui.util.*;
import com.gildedgames.orbis.client.gui.util.directory.GuiDirectoryViewer;
import com.gildedgames.orbis.client.gui.util.directory.nodes.OrbisNavigatorNodeFactory;
import com.gildedgames.orbis.client.gui.util.directory.nodes.ProjectNode;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestCreateProject;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestProjectListing;
import com.gildedgames.orbis.common.network.packets.projects.PacketSaveWorldObjectToProject;
import com.gildedgames.orbis.common.util.InputHelper;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import java.io.File;
import java.io.IOException;

public class GuiSaveBlueprint extends GuiFrame implements IDirectoryNavigatorListener
{
	private final Blueprint blueprint;

	private GuiText title;

	private GuiInput nameInput;

	private GuiButtonVanilla saveButton, backButton;

	private GuiDirectoryViewer directoryViewer;

	private boolean requestListing = true;

	private IProject project;

	private boolean inProjectDirectory;

	public GuiSaveBlueprint(GuiFrame prevFrame, final Blueprint blueprint)
	{
		super(prevFrame, Dim2D.flush());

		this.setDrawDefaultBackground(true);
		this.blueprint = blueprint;
	}

	public void refreshNavigator()
	{
		this.requestListing = false;
		this.directoryViewer.getNavigator().refresh();
		this.requestListing = true;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		GuiButtonVanillaToggled.TOGGLED_BUTTON_ID = -1;
	}

	@Override
	public void init()
	{
		this.title = new GuiText(Dim2D.build().width(140).height(20).addY(3).addX(132).flush(),
				new Text(new TextComponentString("Project Name:"), 1.0F));

		this.nameInput = new GuiInput(Dim2D.build().height(20).addY(15).addX(132).flush());

		this.nameInput.dim().mod().width(this.width - this.nameInput.dim().x() - 20).flush();

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(75).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.backButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.backButton.getInner().displayString = "Back";

		this.addChildren(this.title);
		this.addChildren(this.nameInput);
		this.addChildren(this.saveButton);
		this.addChildren(this.backButton);

		this.directoryViewer = new GuiDirectoryViewer(Pos2D.build().addY(45).addX(20).flush(),
				new DirectoryNavigator(new OrbisNavigatorNodeFactory()));

		this.directoryViewer.dim().mod().width(this.width - this.directoryViewer.dim().x() - 20).flush();
		this.directoryViewer.dim().mod().height(this.height - this.directoryViewer.dim().y() - 20).flush();

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		this.directoryViewer.getNavigator().addListener(this);

		this.directoryViewer.setDisplayBackdrop(false);

		this.directoryViewer.getNavigator().openDirectory(OrbisCore.getProjectManager().getLocation());

		this.addChildren(this.directoryViewer);
	}

	@Override
	public void draw()
	{
		super.draw();

		this.inProjectDirectory = OrbisCore.getProjectManager().getLocation().equals(this.directoryViewer.getNavigator().currentDirectory());

		if (this.inProjectDirectory)
		{
			this.title.setText(new Text(new TextComponentString("Project Name:"), 1.0F));
		}
		else
		{
			this.title.setText(new Text(new TextComponentString("Blueprint Name:"), 1.0F));
		}
	}

	@Override
	protected void keyTypedInner(final char typedChar, final int keyCode) throws IOException
	{
		if (!this.nameInput.getInner().isFocused())
		{
			super.keyTypedInner(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.backButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			if (this.inProjectDirectory)
			{
				final IProjectIdentifier id = new ProjectIdentifier(this.nameInput.getInner().getText(), Minecraft.getMinecraft().player.getName());

				if (!OrbisCore.getProjectManager().projectNameExists(this.nameInput.getInner().getText()) && !OrbisCore.getProjectManager()
						.projectExists(id))
				{
					OrbisAPI.network().sendPacketToServer(new PacketRequestCreateProject(this.nameInput.getInner().getText(), id));
				}
			}
			else
			{
				final File file = new File(this.directoryViewer.getNavigator().currentDirectory(),
						this.nameInput.getInner().getText() + "." + this.blueprint.getData().getFileExtension());

				final String location = file.getCanonicalPath().replace(this.project.getLocationAsFile().getCanonicalPath() + File.separator, "");

				if (Minecraft.getMinecraft().isIntegratedServerRunning())
				{
					try
					{
						final IWorldObject worldObject = this.blueprint;

						if (this.project != null && worldObject.getData() != null && !file.exists())
						{
							IData data = worldObject.getData();

							/**
							 * Check if the state has already been stored.
							 * If so, we should addNew a new identifier for it as
							 * a clone. Many issues are caused if two files use
							 * the same identifier.
							 */
							if (data.getMetadata().getIdentifier() != null && this.project.getCache().hasData(data.getMetadata().getIdentifier().getDataId()))
							{
								data = data.clone();
								data.getMetadata().setIdentifier(this.project.getCache().createNextIdentifier());
							}

							data.preSaveToDisk(worldObject);

							this.project.getCache().setData(data, location);

							this.project.writeData(data, file);
							this.refreshNavigator();
						}
					}
					catch (final OrbisMissingProjectException e)
					{
						OrbisCore.LOGGER.error(e);
					}
				}
				else
				{
					OrbisAPI.network().sendPacketToServer(new PacketSaveWorldObjectToProject(this.project, this.blueprint, location));
				}
			}
		}
	}

	@Override
	public void onNodeClick(IDirectoryNavigator navigator, IDirectoryNode node)
	{

	}

	@Override
	public void onNodeOpen(final IDirectoryNavigator navigator, final IDirectoryNode node)
	{
		if (node instanceof ProjectNode)
		{
			final ProjectNode projectNode = (ProjectNode) node;

			this.project = projectNode.getProject();
		}
	}

	@Override
	public void onDirectoryOpen(final IDirectoryNavigator navigator, final File file)
	{

	}

	@Override
	public void onBack(final IDirectoryNavigator navigator)
	{

	}

	@Override
	public void onForward(final IDirectoryNavigator navigator)
	{

	}

	@Override
	public void onRefresh(final IDirectoryNavigator navigator)
	{
		if (!Minecraft.getMinecraft().isIntegratedServerRunning() && this.requestListing)
		{
			OrbisAPI.network().sendPacketToServer(new PacketRequestProjectListing());
		}
	}
}
