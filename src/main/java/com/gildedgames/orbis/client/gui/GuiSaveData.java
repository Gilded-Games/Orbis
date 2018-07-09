package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.OrbisClientCaches;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.util.directory.GuiDirectoryViewer;
import com.gildedgames.orbis.client.gui.util.directory.nodes.NavigatorNodeProject;
import com.gildedgames.orbis.client.gui.util.directory.nodes.OrbisNavigatorNodeFactory;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestCreateProject;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestProjectListing;
import com.gildedgames.orbis.common.network.packets.projects.PacketSaveWorldObjectToProject;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.data.directory.DirectoryNavigator;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNavigatorListener;
import com.gildedgames.orbis_api.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.gui.util.GuiInput;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.IGuiFrame;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanillaToggled;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
import com.gildedgames.orbis_api.data.management.impl.ProjectIdentifier;
import com.gildedgames.orbis_api.util.InputHelper;
import com.gildedgames.orbis_api.world.IWorldObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import java.io.File;
import java.io.IOException;

public class GuiSaveData extends GuiFrame implements IDirectoryNavigatorListener
{
	private final IWorldObject worldObject;

	private final IData data;

	private GuiText title;

	private GuiInput nameInput;

	private GuiButtonVanilla saveButton, backButton;

	private GuiDirectoryViewer directoryViewer;

	private boolean requestListing = true;

	private IProject project;

	private boolean inProjectDirectory;

	private String viewOnlyDataType;

	public GuiSaveData(IGuiFrame prevFrame, final IData data, String viewOnlyDataType)
	{
		super(prevFrame, Dim2D.flush());

		this.setDrawDefaultBackground(true);
		this.worldObject = null;
		this.viewOnlyDataType = viewOnlyDataType;
		this.data = data;
	}

	public GuiSaveData(IGuiFrame prevFrame, final IWorldObject worldObject, String viewOnlyDataType)
	{
		super(prevFrame, Dim2D.flush());

		this.setDrawDefaultBackground(true);
		this.worldObject = worldObject;
		this.viewOnlyDataType = viewOnlyDataType;
		this.data = null;
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
		this.dim().mod().width(this.width).height(this.height).flush();

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
			this.title.setText(new Text(new TextComponentString("Data Name:"), 1.0F));
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

		if (InputHelper.isHoveredAndTopElement(this.backButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame().getActualScreen());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHoveredAndTopElement(this.saveButton) && mouseButton == 0)
		{
			if (this.inProjectDirectory)
			{
				final IProjectIdentifier id = new ProjectIdentifier(this.nameInput.getInner().getText(), Minecraft.getMinecraft().player.getName());

				if (!OrbisCore.getProjectManager().projectNameExists(this.nameInput.getInner().getText()) && !OrbisCore.getProjectManager()
						.projectExists(id))
				{
					OrbisCore.network().sendPacketToServer(new PacketRequestCreateProject(this.nameInput.getInner().getText(), id));
				}
			}
			else
			{
				IData data = this.worldObject != null ? this.worldObject.getData() : this.data;

				final File file = new File(this.directoryViewer.getNavigator().currentDirectory(),
						this.nameInput.getInner().getText() + "." + data.getFileExtension());

				final String location = file.getCanonicalPath().replace(this.project.getLocationAsFile().getCanonicalPath() + File.separator, "");

				if (this.isOverwriting(file, location))
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiSaveCallback(this, file, location));
				}
				else
				{
					this.save(file, location, false);
				}
			}
		}
	}

	public boolean isOverwriting(File file, String location)
	{
		if (Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			return file.exists();
		}

		return false;
	}

	public void save(File file, String location, boolean canOverwrite)
	{
		if (Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			try
			{
				final IWorldObject worldObject = this.worldObject;

				//TODO: Make sure the new data has the same dimensions as the old data if you're overwriting
				if (this.project != null && (this.data != null || worldObject.getData() != null) && (!file.exists() || canOverwrite))
				{
					IData data = worldObject == null ? this.data : worldObject.getData();

					/**
					 * Check if the state has already been stored.
					 * If so, we should addNew a new identifier for it as
					 * a clone. Many issues are caused if two files use
					 * the same identifier.
					 */
					boolean notSameProjectOrNoProject =
							data.getMetadata().getIdentifier().getProjectIdentifier() == null || !data.getMetadata().getIdentifier().getProjectIdentifier()
									.equals(this.project.getProjectIdentifier());

					if (data.getMetadata().getIdentifier() != null && (this.project.getCache().hasData(data.getMetadata().getIdentifier().getDataId())
							&& !canOverwrite) || notSameProjectOrNoProject)
					{
						data = data.clone();

						data.getMetadata().setIdentifier(this.project.getCache().createNextIdentifier());
					}

					if (worldObject != null)
					{
						data.preSaveToDisk(worldObject);
					}

					this.project.getCache().setData(data, location);

					this.project.writeData(data, file);
					this.refreshNavigator();

					if (canOverwrite && data instanceof BlueprintData)
					{
						OrbisClientCaches.getBlueprintRenders().refresh(data.getMetadata().getIdentifier());
					}
				}
			}
			catch (final OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}
		}
		else
		{
			if (this.worldObject != null)
			{
				OrbisCore.network().sendPacketToServer(new PacketSaveWorldObjectToProject(this.project, this.worldObject, location));
			}
			else
			{
				//TODO: Save this.data, this only works for world objects
			}
		}
	}

	@Override
	public void onNodeClick(IDirectoryNavigator navigator, INavigatorNode node)
	{

	}

	@Override
	public void onNodeOpen(final IDirectoryNavigator navigator, final INavigatorNode node)
	{
		if (node instanceof NavigatorNodeProject)
		{
			final NavigatorNodeProject projectNode = (NavigatorNodeProject) node;

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
			OrbisCore.network().sendPacketToServer(new PacketRequestProjectListing());
		}
	}
}
