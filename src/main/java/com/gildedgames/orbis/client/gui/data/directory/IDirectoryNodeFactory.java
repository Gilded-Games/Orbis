package com.gildedgames.orbis.client.gui.data.directory;

import java.io.File;

public interface IDirectoryNodeFactory
{

	INavigatorNode createFrom(File file, String extension);

}
