package com.gildedgames.orbis.client.gui.data.list;

public interface IListNavigatorListener<T>
{

	void onRemoveNode(T node, int index);

	void onAddNode(T node, int index);

	void onNodeClicked(T node, int index);

}
