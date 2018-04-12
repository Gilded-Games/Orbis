package com.gildedgames.orbis.client.gui.data.list;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import java.util.List;

public class ListNavigator<T> implements IListNavigator<T>
{
	private final List<IListNavigatorListener<T>> listeners = Lists.newArrayList();

	private final BiMap<Integer, T> nodes = HashBiMap.create();

	public ListNavigator()
	{

	}

	@Override
	public void addListener(final IListNavigatorListener<T> listener)
	{
		this.listeners.add(listener);
	}

	@Override
	public boolean removeListener(final IListNavigatorListener<T> listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public void put(final T node, final int index, boolean newNode)
	{
		if (!this.nodes.containsKey(index) || this.nodes.get(index) != node)
		{
			this.nodes.put(index, node);

			this.listeners.forEach(l -> l.onAddNode(node, index, newNode));
		}
	}

	@Override
	public boolean remove(final T node, final int index)
	{
		final boolean flag = this.nodes.remove(index, node);

		this.listeners.forEach(l -> l.onRemoveNode(node, index));

		return flag;
	}

	@Override
	public T remove(int index)
	{
		T node = this.nodes.remove(index);

		this.listeners.forEach(l -> l.onRemoveNode(node, index));

		return node;
	}

	@Override
	public void click(final T node, final int index)
	{
		this.listeners.forEach(l -> l.onNodeClicked(node, index));
	}

	@Override
	public BiMap<Integer, T> getNodes()
	{
		return this.nodes;
	}
}
