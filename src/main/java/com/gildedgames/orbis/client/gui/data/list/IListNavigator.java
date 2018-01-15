package com.gildedgames.orbis.client.gui.data.list;

import java.util.List;

public interface IListNavigator<T>
{

	void addListener(IListNavigatorListener<T> listener);

	boolean removeListener(IListNavigatorListener<T> listener);

	void add(T node, int index);

	void addNew(T node, int index);

	boolean remove(T node, int index);

	void click(T node, int index);

	List<T> getNodes();

}
