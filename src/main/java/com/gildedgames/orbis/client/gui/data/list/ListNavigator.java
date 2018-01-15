package com.gildedgames.orbis.client.gui.data.list;

import com.google.common.collect.Lists;

import java.util.List;

public class ListNavigator<T> implements IListNavigator<T>
{
	private final List<IListNavigatorListener<T>> listeners = Lists.newArrayList();

	private final List<T> nodes = Lists.newArrayList();

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
	public void add(final T node, final int index)
	{
		this.nodes.add(node);

		this.listeners.forEach(l -> l.onAddNode(node, index));
	}

	@Override
	public void addNew(final T node, final int index)
	{
		this.add(node, index);

		this.listeners.forEach(l -> l.onNewNode(node, index));
	}

	@Override
	public boolean remove(final T node, final int index)
	{
		final boolean flag = this.nodes.remove(node);

		this.listeners.forEach(l -> l.onRemoveNode(node, index));

		return flag;
	}

	@Override
	public void click(final T node, final int index)
	{
		this.listeners.forEach(l -> l.onNodeClicked(node, index));
	}

	@Override
	public List<T> getNodes()
	{
		return this.nodes;
	}
}
