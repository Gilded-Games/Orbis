package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.core.tree.INode;

public interface IGuiTreeListener<DATA, LINK, BUTTON extends GuiElement>
{

	void onLinkNodes(GuiTree<DATA, LINK, BUTTON> tree, INode<DATA, LINK> n1, INode<DATA, LINK> n2, LINK link);

	void onClickNode(GuiTree<DATA, LINK, BUTTON> tree, INode<DATA, LINK> node);

	void onAddNode(GuiTree<DATA, LINK, BUTTON> tree, INode<DATA, LINK> node, boolean oldNode);

	void onMoveNode(GuiTree<DATA, LINK, BUTTON> tree, INode<DATA, LINK> node, Pos2D pos);

	void onRemoveNode(GuiTree<DATA, LINK, BUTTON> tree, INode<DATA, LINK> node);

	void onMovePane(GuiTree<DATA, LINK, BUTTON> tree, Pos2D pos);

}