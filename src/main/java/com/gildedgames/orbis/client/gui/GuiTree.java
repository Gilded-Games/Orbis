package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.util.*;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.util.InputHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiTree<DATA, LINK, BUTTON extends GuiFrame> extends GuiFrame
{
	private final static ResourceLocation CROSS = OrbisCore.getResource("navigator/cross.png");

	private final static BufferBuilder buffer = Tessellator.getInstance().getBuffer();

	private Collection<IDropdownElement> canvasDropdownElements, nodeDropdownElements;

	private Pos2D rightClickLocation, leftClickLocation, lastScreenPos;

	private GuiFrame draggableCanvas;

	private Map<INode<DATA, LINK>, BUTTON> nodes = Maps.newHashMap();

	private Function<Integer, INode<DATA, LINK>> nodeFactory;

	private boolean mouseButtonHeld;

	private BUTTON movingButton;

	private INode<DATA, LINK> interactedNode, movingNode;

	private boolean isLinkingNodes;

	private LINK currentLinkData;

	private boolean isDraggingScreen;

	private Supplier<Collection<IDropdownElement>> nodeDropdownElementFactory;

	private Function<LINK, String> linkStringInterpreter;

	private Set<IGuiTreeListener<DATA, LINK, BUTTON>> listeners = Sets.newHashSet();

	private Function<INode<DATA, LINK>, Boolean> nodeValidator;

	private Function<INode<DATA, LINK>, BUTTON> buttonFactory;

	private float movingButtonOffsetX, movingButtonOffsetY;

	public GuiTree(Rect rect, Function<Integer, INode<DATA, LINK>> nodeFactory, Supplier<Collection<IDropdownElement>> nodeDropdownElementFactory,
			Function<LINK, String> linkStringInterpreter, Function<INode<DATA, LINK>, Boolean> nodeValidator, Function<INode<DATA, LINK>, BUTTON> buttonFactory)
	{
		super(rect);

		this.nodeFactory = nodeFactory;
		this.nodeDropdownElementFactory = nodeDropdownElementFactory;
		this.linkStringInterpreter = linkStringInterpreter;
		this.nodeValidator = nodeValidator;
		this.buttonFactory = buttonFactory;

		this.draggableCanvas = new GuiFrameDummy(Dim2D.build().width(this.dim().width()).height(this.dim().height()).flush());
	}

	public BUTTON getButtonFromNode(INode<DATA, LINK> node)
	{
		return this.nodes.get(node);
	}

	public void reset(Pos2D pos)
	{
		this.draggableCanvas.dim().mod().x(0).y(0).flush();
		this.draggableCanvas.clearChildren();
		this.nodes.clear();

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

		this.nodes.put(node, button);

		this.draggableCanvas.addChildren(button);

		this.listeners.forEach((l) -> l.onAddNode(this, node, oldNode));
		this.listeners.forEach((l) -> l.onMoveNode(this, node, pos));
	}

	private void removeNode(INode<DATA, LINK> node)
	{
		this.draggableCanvas.removeChild(this.nodes.get(node));

		this.nodes.remove(node);

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
	public void init()
	{
		this.draggableCanvas = new GuiFrameDummy(Dim2D.build().width(this.dim().width()).height(this.dim().height()).flush());

		this.canvasDropdownElements = Lists.newArrayList(new DropdownElement(new TextComponentString("New Node"))
														 {
															 @Override
															 public void onClick(final GuiDropdownList list, final EntityPlayer player)
															 {
																 INode<DATA, LINK> node = GuiTree.this.nodeFactory.apply(GuiTree.this.nodes.size());

																 GuiTree.this.addNode(node, GuiTree.this.rightClickLocation, false);
															 }
														 },
				GuiRightClickElements.close());

		Collection<IDropdownElement> extraNodeElements = this.nodeDropdownElementFactory.get();

		this.nodeDropdownElements = Lists.newArrayList();

		this.nodeDropdownElements.addAll(extraNodeElements);

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

		this.nodeDropdownElements.add(GuiRightClickElements.close());

		this.addChildren(this.draggableCanvas);
	}

	@Override
	public void draw()
	{
		ScaledResolution res = new ScaledResolution(this.mc);

		double scaleW = this.mc.displayWidth / res.getScaledWidth_double();
		double scaleH = this.mc.displayHeight / res.getScaledHeight_double();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) ((this.dim().x()) * scaleW),
				(int) (this.mc.displayHeight - ((this.dim().y() + this.dim().height()) * scaleH)),
				(int) (this.dim().width() * scaleW), (int) (this.dim().height() * scaleH));

		float mouseX = InputHelper.getMouseX() - this.draggableCanvas.dim().x();
		float mouseY = InputHelper.getMouseY() - this.draggableCanvas.dim().y();

		if (this.mouseButtonHeld && this.movingButton != null)
		{
			this.movingButton.dim().mod().x(mouseX - this.movingButtonOffsetX).y(mouseY - this.movingButtonOffsetY).flush();
		}

		super.draw();

		for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : this.nodes.entrySet())
		{
			INode<DATA, LINK> node = entry.getKey();
			BUTTON button = entry.getValue();

			if (node.getChildrenIds() != null)
			{
				for (INode<DATA, LINK> child : node.getTree().get(node.getChildrenIds()))
				{
					BUTTON childB = this.nodes.get(child);

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
							this.drawCenteredString(this.fontRenderer, TextFormatting.BOLD + linkString, tipX - (dx / 2), tipY - (dy / 2) - 5, 0xFFFFFFFF);
						}
					}
				}
			}
		}

		if (this.isLinkingNodes)
		{
			BUTTON button = this.nodes.get(this.interactedNode);

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
					this.drawCenteredString(this.fontRenderer, TextFormatting.BOLD + linkString, tipX, tipY, 14737632);
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
	protected void mouseClicked(int mouseX, int mouseY, final int mouseButton) throws IOException
	{
		if (!this.isInputEnabled())
		{
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);

		int mx = (int) (mouseX - this.draggableCanvas.dim().x());
		int my = (int) (mouseY - this.draggableCanvas.dim().y());

		for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : this.nodes.entrySet())
		{
			INode<DATA, LINK> node = entry.getKey();
			BUTTON button = entry.getValue();

			if (InputHelper.isHoveredAndTopElement(button))
			{
				if (mouseButton == 1)
				{
					this.interactedNode = node;
					this.rightClickLocation = Pos2D.flush(mx, my);

					IDropdownHolder holder = IDropdownHolder.get();

					if (holder != null)
					{
						holder.getDropdown().display(this.nodeDropdownElements, Pos2D.flush(mouseX, mouseY));
					}
				}
				else
				{
					if (this.isLinkingNodes)
					{
						if (node.hasChild(this.interactedNode.getNodeId()))
						{
							node.removeChild(this.interactedNode.getNodeId());
						}

						this.interactedNode.addChild(node.getNodeId(), this.currentLinkData);
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
				holder.getDropdown().display(this.canvasDropdownElements, Pos2D.flush(mouseX, mouseY));
			}
		}
		else
		{
			if (!this.nodes.isEmpty())
			{
				this.lastScreenPos = Pos2D.flush(this.draggableCanvas.dim().originalState().x(), this.draggableCanvas.dim().originalState().y());
				this.leftClickLocation = Pos2D.flush(mx + this.draggableCanvas.dim().x(), my + this.draggableCanvas.dim().y());

				this.isDraggingScreen = true;
			}
		}
	}

	@Override
	protected void mouseReleased(final int mouseX, final int mouseY, final int state)
	{
		super.mouseReleased(mouseX, mouseY, state);

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
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void preDrawChild(IGuiFrame child)
	{
		ScaledResolution res = new ScaledResolution(this.mc);

		double scaleW = this.mc.displayWidth / res.getScaledWidth_double();
		double scaleH = this.mc.displayHeight / res.getScaledHeight_double();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) ((this.dim().x()) * scaleW),
				(int) (this.mc.displayHeight - ((this.dim().y() + this.dim().height()) * scaleH)),
				(int) (this.dim().width() * scaleW), (int) (this.dim().height() * scaleH));

		//this.drawGradientRect((int) this.dim().x(), (int) this.dim().y(), (int) this.dim().maxX(), (int) this.dim().maxY(), -1072689136, -804253680);
	}

	@Override
	public void postDrawChild(IGuiFrame child)
	{
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (this.isVisible())
		{
			for (Map.Entry<INode<DATA, LINK>, BUTTON> entry : this.nodes.entrySet())
			{
				INode<DATA, LINK> node = entry.getKey();
				BUTTON button = entry.getValue();

				if (this.nodeValidator.apply(node))
				{
					GlStateManager.pushMatrix();

					GuiFrameUtils.applyAlpha(this);

					this.mc.getTextureManager().bindTexture(CROSS);

					GuiTexture.drawModalRectWithCustomSizedTexture(button.dim().x(), button.dim().y(), 0, 0, 9, 9, 9, 9);

					GlStateManager.popMatrix();
				}
			}
		}
	}
}
