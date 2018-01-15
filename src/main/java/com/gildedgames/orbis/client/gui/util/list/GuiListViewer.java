package com.gildedgames.orbis.client.gui.util.list;

import com.gildedgames.orbis.client.gui.data.list.IListNavigator;
import com.gildedgames.orbis.client.gui.data.list.IListNavigatorListener;
import com.gildedgames.orbis.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis.client.gui.util.GuiFactory;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.ModDim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.client.rect.Rect;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.util.InputHelper;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class GuiListViewer<NODE, NODE_GUI extends GuiFrame> extends GuiFrame implements IListNavigatorListener<NODE>
{
	private static final ResourceLocation VIEWER_BACKDROP = OrbisCore.getResource("list/list_viewer.png");

	private final IListNavigator<NODE> navigator;

	private final List<NODE_GUI> visibleGuiNodes = Lists.newArrayList();

	private final List<NODE> visibleNodes = Lists.newArrayList();

	private final List<GuiAbstractButton> visibleDeletes = Lists.newArrayList();

	private final NodeFactory<NODE, NODE_GUI> guiFactory;

	private final Supplier<NODE> nodeFactory;

	private GuiAbstractButton addButton;

	private int currentScroll, maxScroll;

	public GuiListViewer(final Pos2D pos, final IListNavigator<NODE> navigator, final NodeFactory<NODE, NODE_GUI> guiFactory, final Supplier<NODE> nodeFactory)
	{
		super(Dim2D.build().width(176).height(136).pos(pos).flush());

		this.navigator = navigator;
		this.navigator.addListener(this);

		this.guiFactory = guiFactory;
		this.nodeFactory = nodeFactory;
	}

	public NODE_GUI getNodeGui(final int index)
	{
		return this.visibleGuiNodes.get(index - this.currentScroll);
	}

	public IListNavigator<NODE> getNavigator()
	{
		return this.navigator;
	}

	private List<NODE_GUI> listVisibleNodes(final List<NODE> nodes, final int scrollCount, final int xOffset, final int yOffset,
			final int height, final int width, final int nodeHeight,
			final int padding)
	{
		final List<NODE_GUI> guis = Lists.newArrayList();

		final Rect nodeRect = ModDim2D.build().mod().height(nodeHeight).scale(1.0F).flush();

		final int nodeWidthPadded = width - 20 - padding;
		final int nodeHeightPadded = (int) (nodeRect.height() + padding);

		final int possibleNumberOfRows = height / nodeHeightPadded;

		if (nodes.size() < possibleNumberOfRows)
		{
			this.currentScroll = 0;
			this.maxScroll = 0;
		}
		else
		{
			this.maxScroll = nodes.size() - possibleNumberOfRows + 1;
		}

		final int frontNodeIndex = Math.max(0, Math.min(scrollCount, this.maxScroll));
		final int backNodeIndex = Math.min(nodes.size(), frontNodeIndex + possibleNumberOfRows);

		for (int i = frontNodeIndex; i < backNodeIndex; i++)
		{
			final NODE node = nodes.get(i);

			if (node == null)
			{
				continue;
			}

			final int row = i - frontNodeIndex;

			final Pos2D pos = Pos2D.flush(xOffset, yOffset + (row * nodeHeightPadded));

			final NODE_GUI guiNode = this.guiFactory.create(pos, node, i);

			guiNode.dim().mod().width(nodeWidthPadded).height(nodeHeight).scale(nodeRect.scale()).flush();

			guis.add(guiNode);

			this.visibleNodes.add(node);
		}

		if (this.currentScroll == this.maxScroll)
		{
			this.addButton = GuiFactory.createAddButton();

			final Pos2D pos = Pos2D.flush(xOffset + nodeWidthPadded, yOffset + ((backNodeIndex - frontNodeIndex) * nodeHeightPadded));

			this.addButton.dim().mod().x(nodeWidthPadded).pos(pos).scale(nodeRect.scale()).flush();
		}
		else
		{
			this.addButton = null;
		}

		return guis;
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		if (this.isEnabled() && mouseButton == 0)
		{
			if (this.addButton != null && InputHelper.isHovered(this.addButton) && this.addButton.isEnabled())
			{
				this.getNavigator().addNew(this.nodeFactory.get(), this.getNavigator().getNodes().size());
				return;
			}

			for (int i = 0; i < this.visibleDeletes.size(); i++)
			{
				final GuiAbstractButton button = this.visibleDeletes.get(i);
				final GuiFrame nodeGui = this.visibleGuiNodes.get(i);

				final NODE node = this.visibleNodes.get(i);

				if (InputHelper.isHovered(button) && button.isEnabled())
				{
					this.getNavigator().remove(node, i + this.currentScroll);

					return;
				}
				else if (InputHelper.isHovered(nodeGui) && nodeGui.isEnabled())
				{
					this.getNavigator().click(node, i + this.currentScroll);
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void refreshNodes()
	{
		if (this.addButton != null)
		{
			this.removeChild(this.addButton);
		}

		this.visibleNodes.clear();

		final List<NODE_GUI> guiNodes = this
				.listVisibleNodes(this.navigator.getNodes(), this.currentScroll, 8, 8, (int) this.dim().height() - 16, (int) (this.dim().width() - 16), 20, 0);

		this.visibleDeletes.forEach(this::removeChild);
		this.visibleGuiNodes.forEach(this::removeChild);

		this.visibleGuiNodes.clear();
		this.visibleDeletes.clear();

		this.visibleGuiNodes.addAll(guiNodes);
		this.visibleGuiNodes.forEach(this::addChild);

		this.visibleGuiNodes.forEach(g -> {
			final GuiAbstractButton deleteButton = GuiFactory.createDeleteButton();

			deleteButton.dim().mod().pos(Pos2D.flush(g.dim().originalState().x(), g.dim().originalState().y())).addX(g.dim().width()).flush();

			this.visibleDeletes.add(deleteButton);
			this.addChild(deleteButton);
		});

		if (this.addButton != null)
		{
			this.addChild(this.addButton);
		}
	}

	@Override
	public void onMouseWheel(final int state)
	{
		super.onMouseWheel(state);

		this.currentScroll = Math.max(0, Math.min(this.maxScroll, this.currentScroll - (state / 120)));

		this.refreshNodes();
	}

	@Override
	public void init()
	{
		final GuiTexture backdrop = new GuiTexture(Dim2D.buildWith(this).area().flush(), VIEWER_BACKDROP);

		this.addChild(backdrop);

		this.refreshNodes();
	}

	@Override
	public void onRemoveNode(final NODE node, final int index)
	{
		this.refreshNodes();
	}

	@Override
	public void onAddNode(final NODE node, final int index)
	{
		this.refreshNodes();
	}

	@Override
	public void onNewNode(final NODE node, final int index)
	{

	}

	@Override
	public void onNodeClicked(final NODE node, final int index)
	{

	}

	public interface NodeFactory<N, G>
	{

		G create(Pos2D pos, N node, int index);

	}
}
