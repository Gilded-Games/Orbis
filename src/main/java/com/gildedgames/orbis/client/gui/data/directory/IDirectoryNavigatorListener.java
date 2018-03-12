package com.gildedgames.orbis.client.gui.data.directory;

import java.io.File;

public interface IDirectoryNavigatorListener
{

	void onNodeClick(IDirectoryNavigator navigator, INavigatorNode node);

	void onNodeOpen(IDirectoryNavigator navigator, INavigatorNode node);

	void onDirectoryOpen(IDirectoryNavigator navigator, File file);

	void onBack(IDirectoryNavigator navigator);

	void onForward(IDirectoryNavigator navigator);

	void onRefresh(IDirectoryNavigator navigator);

}
