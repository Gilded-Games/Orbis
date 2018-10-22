package com.gildedgames.orbis.common.variables.displays;

import com.gildedgames.orbis.client.gui.util.directory.GuiDirectoryViewer;
import com.gildedgames.orbis.client.gui.util.directory.nodes.NavigatorNodeProject;
import com.gildedgames.orbis.client.gui.util.directory.nodes.OrbisNavigatorNodeFactory;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestProjectListing;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.client.gui.data.directory.DirectoryNavigator;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNavigatorListener;
import com.gildedgames.orbis_api.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiViewer;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanillaToggled;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IProject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class GuiViewProjects extends GuiViewer implements IDirectoryNavigatorListener
{
	private GuiText title;

	private GuiButtonVanilla backButton;

	private GuiDirectoryViewer directoryViewer;

	private boolean requestListing = true;

	private IProject project;

	private boolean inProjectDirectory;

	private Function<String, Boolean> extensionValidator;

	private Function<IData, Boolean> onDoubleClickFile;

	public GuiViewProjects(IGuiViewer prevViewer, Function<String, Boolean> extensionValidator, Function<IData, Boolean> onDoubleClickFile)
	{
		super(new GuiElement(Dim2D.flush(), false), prevViewer);

		this.extensionValidator = extensionValidator;
		this.onDoubleClickFile = onDoubleClickFile;
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
	public void build(IGuiContext context)
	{
		this.backButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.backButton.getInner().displayString = "Back";

		context.addChildren(this.backButton);

		this.directoryViewer = new GuiDirectoryViewer(Pos2D.build().addY(45).addX(20).flush(),
				new DirectoryNavigator(new OrbisNavigatorNodeFactory(this.extensionValidator)));

		this.directoryViewer.dim().mod().width(this.getScreenWidth() - this.directoryViewer.dim().x() - 20).flush();
		this.directoryViewer.dim().mod().height(this.getScreenHeight() - this.directoryViewer.dim().y() - 20).flush();

		if (!OrbisAPI.services().getProjectManager().getLocation().exists())
		{
			if (!OrbisAPI.services().getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		this.directoryViewer.getNavigator().addListener(this);

		this.directoryViewer.setDisplayBackdrop(false);

		this.directoryViewer.getNavigator().openDirectory(OrbisAPI.services().getProjectManager().getLocation());

		this.directoryViewer.getNavigator()
				.injectDirectories(OrbisCore.getProjectManager().getLocation(), OrbisCore.getProjectManager().getExtraProjectSourceFolders());

		context.addChildren(this.directoryViewer);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.backButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			this.mc.displayGuiScreen(this.getPreviousViewer().getActualScreen());
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
		else
		{
			try
			{
				final Optional<IData> data = OrbisCore.getProjectManager().findData(this.project, node.getFile());

				if (!data.isPresent())
				{
					OrbisCore.LOGGER.info("Could not load data: " + node.getFile() + " - Project: " + this.project);
					return;
				}

				if (this.onDoubleClickFile.apply(data.get()))
				{
					this.mc.displayGuiScreen(this.getPreviousViewer().getActualScreen());
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}
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
