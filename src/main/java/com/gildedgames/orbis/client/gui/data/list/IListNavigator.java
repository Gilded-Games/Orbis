package com.gildedgames.orbis.client.gui.data.list;

import com.google.common.collect.BiMap;

public interface IListNavigator<T>
{

	void addListener(IListNavigatorListener<T> listener);

	boolean removeListener(IListNavigatorListener<T> listener);

	void put(T node, int index, boolean newNode);

	boolean remove(T node, int index);

	T remove(int index);

	void click(T node, int index);

	BiMap<Integer, T> getNodes();

}
