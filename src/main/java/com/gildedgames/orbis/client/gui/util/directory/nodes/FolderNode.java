package com.gildedgames.orbis.client.gui.util.directory.nodes;

import com.gildedgames.orbis.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis.client.gui.data.directory.IDirectoryNode;
import com.gildedgames.orbis.client.gui.util.GuiFactory;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class FolderNode implements IDirectoryNode
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("navigator/folder.png");

	private final File file;

	private final GuiTexture icon = new GuiTexture(Dim2D.build().area(23).flush(), TEXTURE);

	public FolderNode(final File file)
	{
		if (!file.isDirectory())
		{
			throw new RuntimeException("File given to FolderNode is not a directory! Aborting.");
		}

		this.file = file;
	}

	@Override
	public File getFile()
	{
		return this.file;
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean isOnClient()
	{
		return true;
	}

	@Override
	public boolean isDownloading()
	{
		return false;
	}

	@Override
	public void onOpen(final IDirectoryNavigator navigator)
	{
		navigator.openDirectory(this.file);
	}

	@Override
	public void onDelete(final IDirectoryNavigator navigator)
	{

	}

	@Override
	public Collection<IDropdownElement> getRightClickElements(final IDirectoryNavigator navigator)
	{
		final List<IDropdownElement> elements = Lists.newArrayList();

		elements.add(GuiFactory.createDeleteFileDropdownElement(this.file, navigator));
		elements.add(GuiFactory.createCloseDropdownElement(this.file, navigator));

		return elements;
	}
}
