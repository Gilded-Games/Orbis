package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IProject;
import com.gildedgames.orbis.api.data.management.IProjectIdentifier;
import com.gildedgames.orbis.api.data.management.impl.ProjectIdentifier;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.ScheduleDataType;
import com.gildedgames.orbis.api.data.schedules.ScheduleLayer;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.data.directory.DirectoryNavigator;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNavigatorListener;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNode;
import com.gildedgames.orbis.client.gui.data.list.ListNavigator;
import com.gildedgames.orbis.client.gui.util.*;
import com.gildedgames.orbis.client.gui.util.directory.GuiDirectoryViewer;
import com.gildedgames.orbis.client.gui.util.directory.nodes.OrbisNavigatorNodeFactory;
import com.gildedgames.orbis.client.gui.util.directory.nodes.ProjectNode;
import com.gildedgames.orbis.client.gui.util.list.GuiListViewer;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintAddScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintRemoveScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintSetCurrentScheduleLayer;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestCreateProject;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestProjectListing;
import com.gildedgames.orbis.common.network.packets.projects.PacketSaveWorldObjectToProject;
import com.gildedgames.orbis.common.util.InputHelper;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import java.io.File;
import java.io.IOException;

public class GuiEditBlueprint extends GuiFrame implements IDirectoryNavigatorListener
{
	private final Blueprint blueprint;

	private GuiText title;

	private GuiInput nameInput;

	private GuiButtonVanilla saveButton, closeButton;

	private GuiDirectoryViewer directoryViewer;

	private GuiListViewer<IScheduleLayer, GuiButtonVanillaToggled> layerViewer;

	private boolean requestListing = true;

	private IProject project;

	private boolean inProjectDirectory;

	public GuiEditBlueprint(final Blueprint blueprint)
	{
		super(Dim2D.flush());

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
		final Pos2D center = Pos2D.flush((this.width / 2) + 100, this.height / 2);

		final int yOffset = -80;

		this.title = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(-25).addX(-70).addY(yOffset).flush(),
				new Text(new TextComponentString("Project Name:"), 1.0F));

		this.nameInput = new GuiInput(Dim2D.build().center(true).width(140).height(20).pos(center).addY(yOffset).flush());

		this.saveButton = new GuiButtonVanilla(Dim2D.build().center(true).width(50).height(20).pos(center).addY(30).addX(-30).addY(yOffset).flush());

		this.saveButton.getInner().displayString = "Save";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().center(true).width(50).height(20).pos(center).addY(30).addX(30).addY(yOffset).flush());

		this.closeButton.getInner().displayString = "Close";

		this.addChild(this.title);
		this.addChild(this.nameInput);
		this.addChild(this.saveButton);
		this.addChild(this.closeButton);

		this.directoryViewer = new GuiDirectoryViewer(center.clone().addX(-200).flush(),
				new DirectoryNavigator(new OrbisNavigatorNodeFactory()));
		this.layerViewer = new GuiListViewer<IScheduleLayer, GuiButtonVanillaToggled>(center.clone().addY(40).flush(), new ListNavigator<>(), (p, n, i) ->
		{
			final GuiButtonVanillaToggled button = new GuiButtonVanillaToggled(Dim2D.build().pos(p).width(130).height(20).flush(), i);

			button.getInner().displayString = n.displayName();

			return button;
		}, () -> new ScheduleLayer("Layer " + String.valueOf(GuiEditBlueprint.this.blueprint.getData().getScheduleLayers().size() + 1),
				GuiEditBlueprint.this.blueprint, ScheduleDataType.FILL))
		{
			@Override
			public void onNewNode(final IScheduleLayer node, final int index)
			{
				super.onAddNode(node, index);

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				if (this.mc.isIntegratedServerRunning())
				{
					b.getData().addScheduleLayer(node);
				}
				else
				{
					if (b.getData().getMetadata().getIdentifier() == null)
					{
						NetworkingOrbis.sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b, node.displayName()));
					}
					else
					{
						NetworkingOrbis.sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b.getData().getMetadata().getIdentifier(), node.displayName()));
					}
				}
			}

			@Override
			public void onRemoveNode(final IScheduleLayer node, final int index)
			{
				super.onRemoveNode(node, index);

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				if (b.getData().getMetadata().getIdentifier() == null)
				{
					NetworkingOrbis.sendPacketToServer(
							new PacketBlueprintRemoveScheduleLayer(b, b.getData().getIndexOfScheduleLayer(node)));
				}
				else
				{
					NetworkingOrbis.sendPacketToServer(
							new PacketBlueprintRemoveScheduleLayer(b.getData().getMetadata().getIdentifier(), b.getData().getIndexOfScheduleLayer(node)));
				}
			}

			@Override
			public void onNodeClicked(final IScheduleLayer node, final int index)
			{
				super.onNodeClicked(node, index);

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				final int layerIndex = b.getData().getIndexOfScheduleLayer(node);

				if (layerIndex != -1)
				{
					NetworkingOrbis.sendPacketToServer(new PacketBlueprintSetCurrentScheduleLayer(b, layerIndex));
				}
				else
				{
					OrbisCore.LOGGER.error("Layer index is -1 while trying to click on a node in GuiEditBlueprint.");
				}
			}
		};

		this.directoryViewer.dim().mod().center(true).flush();
		this.layerViewer.dim().mod().center(true).flush();

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		for (int i = 0; i < this.blueprint.getData().getScheduleLayers().size(); i++)
		{
			final IScheduleLayer layer = this.blueprint.getData().getScheduleLayers().get(i);

			this.layerViewer.getNavigator().add(layer, i);
		}

		this.directoryViewer.getNavigator().addListener(this);

		this.directoryViewer.getNavigator().openDirectory(OrbisCore.getProjectManager().getLocation());

		this.addChild(this.directoryViewer);
		this.addChild(this.layerViewer);
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

		if (InputHelper.isHovered(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			GuiRightClickBlueprint.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			if (this.inProjectDirectory)
			{
				final IProjectIdentifier id = new ProjectIdentifier(this.nameInput.getInner().getText(), Minecraft.getMinecraft().player.getName());

				if (!OrbisCore.getProjectManager().projectNameExists(this.nameInput.getInner().getText()) && !OrbisCore.getProjectManager()
						.projectExists(id))
				{
					NetworkingOrbis.sendPacketToServer(new PacketRequestCreateProject(this.nameInput.getInner().getText(), id));
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
					NetworkingOrbis.sendPacketToServer(new PacketSaveWorldObjectToProject(this.project, this.blueprint, location));
				}
			}
		}
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
			NetworkingOrbis.sendPacketToServer(new PacketRequestProjectListing());
		}
	}
}
