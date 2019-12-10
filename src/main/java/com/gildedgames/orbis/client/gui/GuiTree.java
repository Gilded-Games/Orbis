package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.client.gui.util.GuiFrameUtils;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.IDropdownHolder;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiLibHelper;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiEvent;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.client.rect.Rect;
import com.gildedgames.orbis.lib.client.rect.RectModifier;
import com.gildedgames.orbis.lib.core.tree.INode;
import com.gildedgames.orbis.lib.util.InputHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiTree<DATA, LINK, BUTTON extends GuiElement> extends GuiElement
{
	private final static ResourceLocation CROSS = OrbisCore.getResource("navigator/cross.png");

	private final static BufferBuilder buffer = Tessellator.getInstance().getBuffer();

	private Collection<IDropdownElement> canvasDropdownElements, nodeDropdownElements;

	private Pos2D rightClickLocation, leftClickLocation, lastScreenPos;

	private GuiElement draggableCanvas;

	private Map<INode<DATA, LINK>, BUTTON> nodeToButton = Maps.newHashMap();

	private Map<BUTTON, INode<DATA, LINK>> buttonToNode = Maps.newHashMap();

	private Function<Integer, INode<DATA, LINK>> nodeFactory;

	private boolean mouseButtonHeld;

	private BUTTON movingButton;

	private INode<DATA, LINK> interactedNode, movingNode;

	private boolean isLinkingNodes;

	private LINK currentLinkData;

	private boolean isDraggingScreen;

	private Function<INode<DATA, LINK>, Collection<IDropdownElement>> nodeDropdownElementFactory;

	private Function<LINK, String> linkStringInterpreter;

	private Set<IGuiTreeListener<DATA, LINK, BUTTON>> listeners = Sets.newHashSet();

	private Function<INode<DATA, LINK>, Boolean> nodeValidator;

	private Function<INode<DATA, LINK>, BUTTON> buttonFactory;

	private Function<INode<DATA, LINK>, Boolean> canDeleteNode;

	private Supplier<Integer> nodeIdFactory;

	private float movingButtonOffsetX, movingButtonOffsetY;

	private INode<DATA, LINK> copiedNode;

	private GuiElement invalidRenderer = new GuiElement(Dim2D.flush(), true)
	{
		@Override
		public void onDraw(GuiElement element)
		{
			for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : GuiTree.this.nodeToButton.entrySet())
			{
				INode<DATA, LINK> node = entry.getKey();
				BUTTON button = entry.getValue();

				if (!GuiTree.this.nodeValidator.apply(node))
				{
					GlStateManager.pushMatrix();

					GuiFrameUtils.applyAlpha(this.state());

					this.viewer().mc().getTextureManager().bindTexture(CROSS);

					GuiTexture.drawModalRectWithCustomSizedTexture(button.dim().x(), button.dim().y(), 0, 0, 9, 9, 9, 9);

					GlStateManager.popMatrix();
				}
			}
		}
	};

	private IGuiEvent<IGuiElement> scissorEvent = new IGuiEvent<IGuiElement>()
	{
		@Override
		public void onPreDraw(IGuiElement element)
		{
			ScaledResolution res = new ScaledResolution(GuiTree.this.viewer().mc());

			double scaleW = GuiTree.this.viewer().mc().displayWidth / res.getScaledWidth_double();
			double scaleH = GuiTree.this.viewer().mc().displayHeight / res.getScaledHeight_double();

			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor((int) ((GuiTree.this.dim().x()) * scaleW),
					(int) (GuiTree.this.viewer().mc().displayHeight - ((GuiTree.this.dim().y() + GuiTree.this.dim().height()) * scaleH)),
					(int) (GuiTree.this.dim().width() * scaleW), (int) (GuiTree.this.dim().height() * scaleH));
		}

		@Override
		public void onPostDraw(IGuiElement element)
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		@Override
		public boolean isMouseClickedEnabled(IGuiElement element, int mouseX, int mouseY, int mouseButton)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}

		@Override
		public boolean isMouseClickMoveEnabled(IGuiElement element, final int mouseX, final int mouseY, final int clickedMouseButton,
				final long timeSinceLastClick)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}

		@Override
		public boolean isMouseReleasedEnabled(IGuiElement element, final int mouseX, final int mouseY, final int state)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}

		@Override
		public boolean isMouseWheelEnabled(IGuiElement element, final int state)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}

		@Override
		public boolean isHandleMouseClickEnabled(IGuiElement element, final Slot slotIn, final int slotId, final int mouseButton, final ClickType type)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}

		@Override
		public boolean canBeHovered(IGuiElement element)
		{
			return element == GuiTree.this || GuiTree.this.state().isHovered();
		}
	};

	public GuiTree(Rect rect, Function<Integer, INode<DATA, LINK>> nodeFactory,
			Function<INode<DATA, LINK>, Collection<IDropdownElement>> nodeDropdownElementFactory,
			Function<LINK, String> linkStringInterpreter, Function<INode<DATA, LINK>, Boolean> nodeValidator, Function<INode<DATA, LINK>, BUTTON> buttonFactory,
			Supplier<Integer> nodeIdFactory)
	{
		super(rect, true);

		this.nodeFactory = nodeFactory;
		this.nodeDropdownElementFactory = nodeDropdownElementFactory;
		this.linkStringInterpreter = linkStringInterpreter;
		this.nodeValidator = nodeValidator;
		this.buttonFactory = buttonFactory;
		this.nodeIdFactory = nodeIdFactory;

		this.draggableCanvas = new GuiElement(Dim2D.build().width(this.dim().width()).height(this.dim().height()).flush(), false);
	}

	public void setCanDeleteNode(Function<INode<DATA, LINK>, Boolean> canDeleteNode)
	{
		this.canDeleteNode = canDeleteNode;
	}

	public BUTTON getButtonFromNode(INode<DATA, LINK> node)
	{
		return this.nodeToButton.get(node);
	}

	public INode<DATA, LINK> getNodeFromButton(BUTTON button)
	{
		return this.buttonToNode.get(button);
	}

	public void reset(Pos2D pos)
	{
		this.draggableCanvas.dim().mod().x(0).y(0).flush();
		this.draggableCanvas.context().clearChildren();
		this.nodeToButton.clear();
		this.buttonToNode.clear();

		this.draggableCanvas.dim().mod().pos(pos).flush();
	}

	public void listen(IGuiTreeListener<DATA, LINK, BUTTON> listener)
	{
		this.listeners.add(listener);
	}

	public void startLinking(LINK link)
	{
		this.currentLinkData = link;
		this.isLinkingNodes = true;
	}

	public void addNode(INode<DATA, LINK> node, Pos2D pos, boolean oldNode)
	{
		BUTTON button = this.buttonFactory.apply(node);

		button.dim().mod().pos(pos).flush();

		this.nodeToButton.put(node, button);
		this.buttonToNode.put(button, node);

		this.draggableCanvas.context().addChildren(button);

		this.listeners.forEach((l) -> l.onAddNode(this, node, oldNode));
		this.listeners.forEach((l) -> l.onMoveNode(this, node, pos));
	}

	private void removeNode(INode<DATA, LINK> node)
	{
		this.draggableCanvas.context().removeChild(this.nodeToButton.get(node));

		BUTTON button = this.nodeToButton.remove(node);
		this.buttonToNode.remove(button);

		this.listeners.forEach((l) -> l.onRemoveNode(this, node));
	}

	private void drawLine(double x1, double y1, double x2, double y2)
	{
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(x1, y1, 0).color(255, 255, 255, 255).endVertex();
		buffer.pos(x2, y2, 0).color(255, 255, 255, 255).endVertex();

		Tessellator.getInstance().draw();

		GlStateManager.enableTexture2D();
	}

	@Override
	public void build()
	{
		this.draggableCanvas = new GuiElement(Dim2D.flush(), false);

		if (!this.draggableCanvas.dim().containsModifier("area"))
		{
			this.draggableCanvas.dim().add(new RectModifier("area", this, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
		}

		this.invalidRenderer.state().setZOrder(5);

		if (!this.invalidRenderer.dim().containsModifier("area"))
		{
			this.invalidRenderer.dim().add(new RectModifier("area", this, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
		}

		this.refreshCanvasDropdownElements();
		this.refreshRightClickNodeElements(null);

		this.context().addChildren(this.draggableCanvas, this.invalidRenderer);

		this.state().setCanBeTopHoverElement(true);

		this.state().addEvent(this.scissorEvent);
	}

	private void refreshCanvasDropdownElements()
	{
		this.canvasDropdownElements = Lists.newArrayList();

		this.canvasDropdownElements.add(new DropdownElement(new TextComponentString("New Node"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				INode<DATA, LINK> node = GuiTree.this.nodeFactory.apply(GuiTree.this.nodeIdFactory.get());

				GuiTree.this.addNode(node, GuiTree.this.rightClickLocation, false);
			}
		});

		if (this.copiedNode != null)
		{
			this.canvasDropdownElements.add(new DropdownElement(new TextComponentString("Paste"))
			{
				@Override
				public void onClick(final GuiDropdownList list, final EntityPlayer player)
				{
					INode<DATA, LINK> node = GuiTree.this.copiedNode.deepClone();

					node.clearLocalLinks();
					node.setNodeId(GuiTree.this.nodeIdFactory.get());

					GuiTree.this.addNode(node, GuiTree.this.rightClickLocation, false);
				}
			});
		}

		this.canvasDropdownElements.add(GuiRightClickElements.close());
	}

	private void refreshRightClickNodeElements(INode<DATA, LINK> node)
	{
		Collection<IDropdownElement> extraNodeElements = node == null ? Collections.emptyList() : this.nodeDropdownElementFactory.apply(node);

		this.nodeDropdownElements = Lists.newArrayList();

		this.nodeDropdownElements.addAll(extraNodeElements);

		if (GuiTree.this.canDeleteNode == null || this.interactedNode == null || GuiTree.this.canDeleteNode.apply(GuiTree.this.interactedNode))
		{
			this.nodeDropdownElements.add(new DropdownElement(new TextComponentString("Delete"))
			{
				@Override
				public void onClick(final GuiDropdownList list, final EntityPlayer player)
				{
					if (GuiTree.this.interactedNode != null)
					{
						GuiTree.this.removeNode(GuiTree.this.interactedNode);
						GuiTree.this.interactedNode = null;
					}
				}
			});
		}

		this.nodeDropdownElements.add(new DropdownElement(new TextComponentString("Copy"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				if (GuiTree.this.interactedNode != null)
				{
					GuiTree.this.copiedNode = GuiTree.this.interactedNode;
				}
			}
		});

		this.nodeDropdownElements.add(GuiRightClickElements.close());
	}

	@Override
	public void onDraw(GuiElement element)
	{
		float mouseX = InputHelper.getMouseX() - this.draggableCanvas.dim().x();
		float mouseY = InputHelper.getMouseY() - this.draggableCanvas.dim().y();

		if (this.mouseButtonHeld && this.movingButton != null)
		{
			this.movingButton.dim().mod().x(mouseX - this.movingButtonOffsetX).y(mouseY - this.movingButtonOffsetY).flush();
		}

		for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : this.nodeToButton.entrySet())
		{
			INode<DATA, LINK> node = entry.getKey();
			BUTTON button = entry.getValue();

			if (node.getChildrenIds() != null)
			{
				for (INode<DATA, LINK> child : node.getTree().get(node.getChildrenIds()))
				{
					BUTTON childB = this.nodeToButton.get(child);

					if (childB != null)
					{
						GlStateManager.pushMatrix();

						GlStateManager.glLineWidth(5.0F);

						float width = button.dim().width();
						float height = button.dim().height();

						float widthChild = childB.dim().width();
						float heightChild = childB.dim().height();

						int tipX = (int) (childB.dim().x() + (widthChild / 2));
						int tipY = (int) (childB.dim().y() + (heightChild / 2));

						int tailX = (int) (button.dim().x() + (width / 2));
						int tailY = (int) (button.dim().y() + (height / 2));

						this.drawLine(tailX, tailY, tipX, tipY);

						int arrowLength = 7;

						int dx = tipX - tailX;
						int dy = tipY - tailY;

						double theta = Math.atan2(dy, dx);

						double rad = Math.toRadians(35);

						double x = tipX - arrowLength * Math.cos(theta + rad);
						double y = tipY - arrowLength * Math.sin(theta + rad);

						double phi2 = Math.toRadians(-35);

						double x2 = tipX - arrowLength * Math.cos(theta + phi2);
						double y2 = tipY - arrowLength * Math.sin(theta + phi2);

						if (!node.isDirectionless())
						{
							this.drawLine(tipX - (dx / 4), tipY - (dy / 4), x - (dx / 4), y - (dy / 4));
							this.drawLine(tipX - (dx / 4), tipY - (dy / 4), x2 - (dx / 4), y2 - (dy / 4));
						}

						GlStateManager.glLineWidth(1.0F);

						GlStateManager.popMatrix();

						String linkString = this.linkStringInterpreter.apply(node.getLinkToChild(child.getNodeId()));

						if (linkString != null && !linkString.isEmpty())
						{
							this.viewer().getActualScreen()
									.drawCenteredString(this.viewer().fontRenderer(), TextFormatting.BOLD + linkString, tipX - (dx / 2), tipY - (dy / 2) - 5,
											0xFFFFFFFF);
						}
					}
				}
			}
		}

		if (this.isLinkingNodes)
		{
			BUTTON button = this.nodeToButton.get(this.interactedNode);

			if (button != null)
			{
				GlStateManager.pushMatrix();

				GlStateManager.glLineWidth(5.0F);

				float width = button.dim().width();
				float height = button.dim().height();

				int tipX = InputHelper.getMouseX();
				int tipY = InputHelper.getMouseY();

				int tailX = (int) (button.dim().x() + (width / 2));
				int tailY = (int) (button.dim().y() + (height / 2));

				this.drawLine(tailX, tailY, tipX, tipY);

				int arrowLength = 7;

				int dx = tipX - tailX;
				int dy = tipY - tailY;

				double theta = Math.atan2(dy, dx);

				double rad = Math.toRadians(35);

				double x = tipX - arrowLength * Math.cos(theta + rad);
				double y = tipY - arrowLength * Math.sin(theta + rad);

				double phi2 = Math.toRadians(-35);

				double x2 = tipX - arrowLength * Math.cos(theta + phi2);
				double y2 = tipY - arrowLength * Math.sin(theta + phi2);

				this.drawLine(tipX, tipY, x, y);
				this.drawLine(tipX, tipY, x2, y2);

				GlStateManager.glLineWidth(1.0F);

				GlStateManager.popMatrix();

				String linkString = this.linkStringInterpreter.apply(this.currentLinkData);

				if (linkString != null && !linkString.isEmpty())
				{
					this.viewer().getActualScreen().drawCenteredString(this.viewer().fontRenderer(), TextFormatting.BOLD + linkString, tipX, tipY, 14737632);
				}
			}
		}

		if (this.isDraggingScreen)
		{
			int difX = InputHelper.getMouseX() - (int) this.leftClickLocation.x();
			int difY = InputHelper.getMouseY() - (int) this.leftClickLocation.y();

			this.draggableCanvas.dim().mod().x(this.lastScreenPos.x() + difX).y(this.lastScreenPos.y() + difY).flush();
		}
	}

	@Override
	public void onMouseClicked(GuiElement element, int mouseX, int mouseY, final int mouseButton)
	{
		int mx = (int) (mouseX - this.draggableCanvas.dim().x());
		int my = (int) (mouseY - this.draggableCanvas.dim().y());

		boolean aNodeIsHovered = false;

		for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : this.nodeToButton.entrySet())
		{
			INode<DATA, LINK> node = entry.getKey();
			BUTTON button = entry.getValue();

			if (InputHelper.isHovered(button))
			{
				aNodeIsHovered = true;
			}

			if (button.state().isHoveredAndTopElement())
			{
				if (mouseButton == 1)
				{
					this.interactedNode = node;
					this.rightClickLocation = Pos2D.flush(mx, my);

					IDropdownHolder holder = IDropdownHolder.get();

					if (holder != null)
					{
						this.refreshRightClickNodeElements(this.interactedNode);

						holder.getDropdown().display(this.nodeDropdownElements, Pos2D.flush(mouseX, mouseY));
					}
				}
				else
				{
					if (this.isLinkingNodes)
					{
						if (this.interactedNode.addChild(node.getNodeId(), this.currentLinkData))
						{
							if (node.hasChild(this.interactedNode.getNodeId()))
							{
								node.removeChild(this.interactedNode.getNodeId());
							}
						}

						this.isLinkingNodes = false;

						this.listeners.forEach((l) -> l.onLinkNodes(this, node, this.interactedNode, this.currentLinkData));
					}
					else
					{
						this.mouseButtonHeld = true;
						this.movingButton = button;
						this.movingNode = node;

						this.movingButtonOffsetX = (mouseX - this.movingButton.dim().x());
						this.movingButtonOffsetY = (mouseY - this.movingButton.dim().y());

						this.listeners.forEach((l) -> l.onClickNode(this, node));
					}
				}

				return;
			}
		}

		if (mouseButton == 1)
		{
			this.rightClickLocation = Pos2D.flush(mx, my);

			IDropdownHolder holder = IDropdownHolder.get();

			if (holder != null)
			{
				this.refreshCanvasDropdownElements();

				holder.getDropdown().display(this.canvasDropdownElements, Pos2D.flush(mouseX, mouseY));
			}
		}
		else
		{
			if (!this.nodeToButton.isEmpty() && !aNodeIsHovered)
			{
				this.lastScreenPos = Pos2D.flush(this.draggableCanvas.dim().originalState().x(), this.draggableCanvas.dim().originalState().y());
				this.leftClickLocation = Pos2D.flush(mx + this.draggableCanvas.dim().x(), my + this.draggableCanvas.dim().y());

				this.isDraggingScreen = true;
			}
		}
	}

	@Override
	public void onMouseReleased(GuiElement element, final int mouseX, final int mouseY, final int state)
	{
		if (this.movingNode != null && this.movingButton != null)
		{
			this.listeners.forEach((l) -> l.onMoveNode(this, this.movingNode, this.movingButton.dim().originalState().min()));
		}

		if (this.isDraggingScreen)
		{
			this.listeners.forEach((l) -> l.onMovePane(this, this.draggableCanvas.dim().originalState().min()));
		}

		this.mouseButtonHeld = false;
		this.movingButton = null;
		this.movingNode = null;
		this.isDraggingScreen = false;
	}

	public boolean isLinkingNodes()
	{
		return this.isLinkingNodes;
	}

	public void setLinkingNodes(boolean linkingNodes)
	{
		this.isLinkingNodes = linkingNodes;
	}

	@Override
	public void onGlobalContextChanged(GuiElement element)
	{
		for (IGuiElement child : GuiLibHelper.getAllChildrenRecursivelyFor(this))
		{
			child.state().addEvent(this.scissorEvent);
		}
	}

	@Override
	public void onPreDraw(GuiElement element)
	{
		for (IGuiElement child : this.draggableCanvas.context().getChildren())
		{
			if (child.dim().maxY() < this.dim().min().y() || child.dim().maxX() < this.dim().min().x() || child.dim().min().x() > this.dim().maxX()
					|| child.dim().min().y() > this.dim().maxY())
			{
				child.state().setVisible(false);
			}
			else
			{
				child.state().setVisible(true);
			}
		}
	}

	@Override
	public void onPostDraw(GuiElement element)
	{

	}
}
