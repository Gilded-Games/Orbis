package com.gildedgames.orbis.client.gui.util.directory;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis.lib.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis.lib.client.gui.util.GuiTextBox;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class GuiDirectoryNode extends GuiElement
{
	private static final ResourceLocation TICK_TEXTURE = OrbisCore.getResource("navigator/tick.png");

	private static final ResourceLocation CROSS_TEXTURE = OrbisCore.getResource("navigator/cross.png");

	private static final ResourceLocation DOWNLOADING_TEXTURE = OrbisCore.getResource("navigator/downloading.png");

	public static int WIDTH = 40, HEIGHT = 54;

	private final INavigatorNode directoryNode;

	private final IDirectoryNavigator navigator;

	private final GuiDirectoryViewer viewer;

	private final GuiTexture tick_icon = new GuiTexture(Dim2D.build().width(10).height(12).flush(), TICK_TEXTURE);

	private final GuiTexture cross_icon = new GuiTexture(Dim2D.build().width(9).height(9).flush(), CROSS_TEXTURE);

	private final GuiTexture downloading_icon = new GuiTexture(Dim2D.build().width(9).height(9).flush(), DOWNLOADING_TEXTURE);

	public long lastClickTime = System.currentTimeMillis();

	private GuiTexture icon;

	private GuiTextBox nameplate;

	public GuiDirectoryNode(final Pos2D pos, final INavigatorNode navigatorNode, final GuiDirectoryViewer viewer)
	{
		super(Dim2D.build().width(WIDTH).height(HEIGHT).pos(pos).flush(), true);

		this.directoryNode = navigatorNode;

		this.viewer = viewer;
		this.navigator = viewer.getNavigator();
	}

	@Override
	public void build()
	{
		this.icon = this.directoryNode.getIcon().clone();

		final File file = this.directoryNode.getFile();

		final ITextComponent text = new TextComponentString(file.getName().replace("." + FilenameUtils.getExtension(file.getName()), ""));

		this.nameplate = new GuiTextBox(Dim2D.build().width(WIDTH).height(10).y(27).x(WIDTH / 2).centerX(true).flush(), true,
				new Text(text, 1.0F));

		this.icon.dim().mod().x(WIDTH / 2).addX(1).centerX(true).flush();

		this.cross_icon.dim().mod().x(WIDTH / 2).addX(3).y(HEIGHT / 2).addY(-11).centerX(true).flush();
		this.tick_icon.dim().mod().x(WIDTH / 2).addX(3).y(HEIGHT / 2).addY(-11).centerX(true).flush();
		this.downloading_icon.dim().mod().x(WIDTH / 2).addX(3).y(HEIGHT / 2).addY(-11).centerX(true).flush();

		this.cross_icon.state().setVisible(false);
		this.tick_icon.state().setVisible(false);
		this.downloading_icon.state().setVisible(false);

		this.context().addChildren(this.icon);
		this.context().addChildren(this.nameplate);

		this.context().addChildren(this.cross_icon);
		this.context().addChildren(this.tick_icon);
		this.context().addChildren(this.downloading_icon);

		this.state().setCanBeTopHoverElement(true);
	}

	@Override
	public void onPreDraw(GuiElement element)
	{
		if (Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			this.tick_icon.state().setVisible(false);
			this.cross_icon.state().setVisible(false);
			this.downloading_icon.state().setVisible(false);
		}
		else
		{
			this.tick_icon.state().setVisible(this.directoryNode.isOnClient());
			this.cross_icon.state().setVisible(!this.directoryNode.isOnClient() && !this.directoryNode.isDownloading());
			this.downloading_icon.state().setVisible(this.directoryNode.isDownloading() && !this.directoryNode.isOnClient());
		}
	}

	@Override
	public void onMouseClicked(GuiElement element, final int mouseX, final int mouseY, final int mouseButton)
	{
		if (this.state().isEnabled() && this.state().isHoveredAndTopElement())
		{
			if (mouseButton == 0)
			{
				this.icon.dim().mod().scale(1.0F).flush();

				final long millisecondsSince = System.currentTimeMillis() - this.lastClickTime;

				this.lastClickTime = System.currentTimeMillis();

				this.navigator.onClickNode(this.directoryNode);

				if (millisecondsSince < 200)
				{
					this.directoryNode.onOpen(this.navigator);
					this.navigator.onOpenNode(this.directoryNode);
				}
			}
		}
	}

	@Override
	public void onMouseReleased(GuiElement element, final int mouseX, final int mouseY, final int state)
	{
		if (this.state().isEnabled() && this.state().isHoveredAndTopElement())
		{
			this.icon.dim().mod().scale(1.025F).flush();
		}
	}

	@Override
	public void onHovered(GuiElement element)
	{
		Gui.drawRect((int) this.dim().x(), (int) this.dim().y(), (int) this.dim().maxX(), (int) this.dim().maxY(), Integer.MAX_VALUE);
	}

	@Override
	public void onHoverEnter(GuiElement element)
	{
		this.icon.dim().mod().scale(1.025F).flush();

		this.viewer.getDropdownList().setDropdownElements(this.directoryNode.getRightClickElements(this.navigator));
	}

	@Override
	public void onHoverExit(GuiElement element)
	{
		this.icon.dim().mod().scale(1.0F).flush();
	}

}
