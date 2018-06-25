package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.util.GuiTab;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintAddScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintRemoveScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintScheduleLayerGuiPos;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintSetCurrentScheduleLayer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.*;
import com.gildedgames.orbis_api.client.gui.util.decorators.GuiScrollable;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.tree.ConditionLink;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.LayerLink;
import com.gildedgames.orbis_api.core.tree.NodeMultiParented;
import com.gildedgames.orbis_api.core.variables.IGuiVar;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionRatio;
import com.gildedgames.orbis_api.core.variables.conditions.IGuiCondition;
import com.gildedgames.orbis_api.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.data.schedules.ScheduleLayer;
import com.gildedgames.orbis_api.util.InputHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiLayerEditor extends GuiFrame implements IDropdownHolder
{
	private static final ResourceLocation CONDITION_WINDOW = OrbisCore.getResource("layer_gui/condition_window.png");

	private static final ResourceLocation LAYERS_ICON = OrbisCore.getResource("blueprint_gui/layers_icon.png");

	private static final ResourceLocation POST_GEN_ICON = OrbisCore.getResource("blueprint_gui/post_gen_icon.png");

	private GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> layerTree;

	private GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> conditionTree;

	private GuiVarDisplay varDisplay;

	private GuiScrollable varDisplayScrollDecorator;

	private GuiTexture conditionWindow;

	private IScheduleLayer currentSelectedLayer;

	private Blueprint blueprint;

	private GuiButtonVanilla saveButton, closeButton;

	private GuiTab layerTab, postGenTab;

	private GuiText selectedLayerTitle, selectedConditionTitle;

	private INode currentSelectedNode;

	private INode<IScheduleLayer, LayerLink> currentSelectedLayerNode;

	private GuiDropdownList dropdown;

	public GuiLayerEditor(Blueprint blueprint)
	{
		this.setDrawDefaultBackground(true);

		this.blueprint = blueprint;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.dropdown = new GuiDropdownList(Pos2D.flush());

		Pos2D center = InputHelper.getCenter();

		this.layerTab = new GuiTab(Dim2D.build().x(center.x()).addX(-11).centerX(true).flush(),
				new GuiTexture(Dim2D.build().width(16).height(16).flush(), LAYERS_ICON), () -> {
		});
		this.postGenTab = new GuiTab(Dim2D.build().x(center.x()).addX(11).centerX(true).flush(),
				new GuiTexture(Dim2D.build().width(16).height(16).flush(), POST_GEN_ICON),
				() -> OrbisCore.network().sendPacketToServer(
						new PacketOpenGui(OrbisGuiHandler.POST_GEN, this.blueprint.getMax().getX(), this.blueprint.getMax().getY(),
								this.blueprint.getMax().getZ())));

		this.layerTab.setPressed(true);

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).x(this.width).addX(-125).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).x(this.width).addX(-70).flush());

		this.closeButton.getInner().displayString = "Close";

		this.conditionWindow = new GuiTexture(Dim2D.build().width(200).height(150).flush(), CONDITION_WINDOW);

		this.layerTree = new GuiTree<>(Dim2D.build().width(this.width - 200).height(this.height - 40).x(200).y(40).flush(), (nodeId) ->
		{
			NodeMultiParented<IScheduleLayer, LayerLink> node = new NodeMultiParented<>(
					new ScheduleLayer("Layer " + String.valueOf(nodeId + 1), this.blueprint), false);

			node.setNodeId(nodeId);

			return node;
		},
				() -> Collections.singleton(new DropdownElement(new TextComponentString("Link Child"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						GuiLayerEditor.this.layerTree.startLinking(LayerLink.DEFAULT);
					}
				}),
				(l) -> "",
				(n) -> false,
				(n) ->
				{
					GuiButtonVanilla button = new GuiButtonVanilla(
							Dim2D.build().width(10 + this.fontRenderer.getStringWidth(n.getData().getOptions().getDisplayNameVar().getData())).height(20)
									.flush());

					button.getInner().displayString = n.getData().getOptions().getDisplayNameVar().getData();

					return button;
				});

		final List<INode<IGuiCondition, ConditionLink>> roots = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> visitedNodes = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> children = Lists.newArrayList();

		this.conditionTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			NodeMultiParented<IGuiCondition, ConditionLink> node = new NodeMultiParented<>(new GuiConditionRatio(), true);

			node.setNodeId(nodeId);

			return node;
		},
				() -> Lists.newArrayList(new DropdownElement(new TextComponentString("And..."))
										 {
											 @Override
											 public void onClick(final GuiDropdownList list, final EntityPlayer player)
											 {
												 GuiLayerEditor.this.conditionTree.startLinking(ConditionLink.AND);
											 }
										 },
						new DropdownElement(new TextComponentString("Or..."))
						{
							@Override
							public void onClick(final GuiDropdownList list, final EntityPlayer player)
							{
								GuiLayerEditor.this.conditionTree.startLinking(ConditionLink.OR);
							}
						}),
				(l) ->
				{
					switch (l.getProperty())
					{
						case AND:
							return "And";
						case OR:
							return "Or";
					}

					return "";
				},
				(n) ->
				{
					roots.clear();
					visitedNodes.clear();

					n.fetchRoots(roots, visitedNodes);

					return (n.getParentsIds().isEmpty() || (GuiLayerEditor.this.currentSelectedLayer != null && !roots
							.contains(GuiLayerEditor.this.currentSelectedLayer
									.getConditionNodeTree().getProminentRoot()))) && n != GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree()
							.getProminentRoot();
				},
				(n) ->
				{
					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(20).height(20).flush());

					button.getInner().displayString = String.valueOf(n.getNodeId());

					return button;
				});

		this.conditionTree.setZOrder(1);

		this.layerTree.setInputDisabledWhenNotHovered(true);
		this.conditionTree.setInputDisabledWhenNotHovered(true);

		this.conditionTree.setVisible(false);
		this.conditionTree.setEnabled(false);

		this.layerTree.listen(new IGuiTreeListener<IScheduleLayer, LayerLink, GuiButtonVanilla>()
		{
			@Override
			public void onLinkNodes(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, INode<IScheduleLayer, LayerLink> n1,
					INode<IScheduleLayer, LayerLink> n2,
					LayerLink layerLink)
			{

			}

			@Override
			public void onClickNode(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, INode<IScheduleLayer, LayerLink> node)
			{
				GuiLayerEditor.this.currentSelectedLayer = node.getData();

				GuiLayerEditor.this.conditionTree.reset(node.getData().getConditionGuiPos());

				for (INode<IGuiCondition, ConditionLink> n : node.getData().getConditionNodeTree().getNodes())
				{
					GuiLayerEditor.this.conditionTree.addNode(n, n.getData().getGuiPos(), true);
				}

				List<IGuiVar> variables = Lists.newArrayList();

				variables.add(node.getData().getOptions().getDisplayNameVar());
				variables.add(node.getData().getOptions().getChoosesPerBlockVar());
				variables.add(node.getData().getOptions().getEdgeNoiseVar());

				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(variables, null);

				final Blueprint b = GuiLayerEditor.this.blueprint;

				final int layerIndex = b.getData().getScheduleLayerTree().get(node);

				if (layerIndex != -1)
				{
					OrbisCore.network().sendPacketToServer(new PacketBlueprintSetCurrentScheduleLayer(b, layerIndex));
				}
				else
				{
					OrbisCore.LOGGER.error("Layer index is -1 while trying to click on a node in GuiSaveData.");
				}

				GuiLayerEditor.this.selectedLayerTitle
						.setText(new Text(
								new TextComponentTranslation(
										TextFormatting.BOLD + "Conditions: " + TextFormatting.RESET + node.getData().getOptions().getDisplayNameVar()
												.getData()),
								1.0F));

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = node;

				GuiLayerEditor.this.selectedConditionTitle
						.setText(new Text(
								new TextComponentTranslation(
										TextFormatting.BOLD + "Variables: " + TextFormatting.RESET + String
												.valueOf(node.getData().getOptions().getDisplayNameVar().getData())),
								1.0F));
			}

			@Override
			public void onAddNode(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, INode<IScheduleLayer, LayerLink> node, boolean oldNode)
			{
				if (oldNode)
				{
					return;
				}

				final Blueprint b = GuiLayerEditor.this.blueprint;

				if (Minecraft.getMinecraft().isIntegratedServerRunning())
				{
					b.getData().getScheduleLayerTree().add(node);
				}
				else
				{
					if (b.getData().getMetadata().getIdentifier() == null)
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b, node.getData().getOptions().getDisplayNameVar().getData()));
					}
					else
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b.getData().getMetadata().getIdentifier(),
										node.getData().getOptions().getDisplayNameVar().getData()));
					}
				}
			}

			@Override
			public void onMoveNode(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, INode<IScheduleLayer, LayerLink> node, Pos2D pos)
			{
				final Blueprint b = GuiLayerEditor.this.blueprint;

				if (Minecraft.getMinecraft().isIntegratedServerRunning())
				{
					node.getData().setGuiPos(pos);
				}
				else
				{
					if (b.getData().getMetadata().getIdentifier() == null)
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintScheduleLayerGuiPos(b, b.getData().getScheduleLayerTree().get(node), pos));
					}
					else
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintScheduleLayerGuiPos(b.getData().getMetadata().getIdentifier(),
										b.getData().getScheduleLayerTree().get(node),
										pos));
					}
				}
			}

			@Override
			public void onRemoveNode(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, INode<IScheduleLayer, LayerLink> node)
			{
				final Blueprint b = GuiLayerEditor.this.blueprint;

				if (Minecraft.getMinecraft().isIntegratedServerRunning())
				{
					b.getData().getScheduleLayerTree().remove(node.getNodeId());
				}
				else
				{
					if (b.getData().getMetadata().getIdentifier() == null)
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintRemoveScheduleLayer(b, b.getData().getScheduleLayerTree().get(node)));
					}
					else
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintRemoveScheduleLayer(b.getData().getMetadata().getIdentifier(),
										b.getData().getScheduleLayerTree().get(node)));
					}
				}

				if (node == GuiLayerEditor.this.currentSelectedNode)
				{
					GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
					GuiLayerEditor.this.varDisplay.reset();

					GuiLayerEditor.this.selectedConditionTitle
							.setText(new Text(
									new TextComponentTranslation(
											TextFormatting.BOLD + "Variables: "),
									1.0F));
				}
			}

			@Override
			public void onMovePane(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiLayerEditor.this.blueprint.getData().setTreeGuiPos(pos);
			}
		});

		this.conditionTree.listen(new IGuiTreeListener<IGuiCondition, ConditionLink, GuiButtonVanilla>()
		{

			@Override
			public void onLinkNodes(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> n1,
					INode<IGuiCondition, ConditionLink> n2,
					ConditionLink conditionLink)
			{

				INode<IGuiCondition, ConditionLink> root = GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().getProminentRoot();

				if (root != null)
				{
					roots.clear();
					visitedNodes.clear();

					root.fetchRoots(roots, visitedNodes);

					if (!roots.contains(root))
					{
						INode<IGuiCondition, ConditionLink> prominentRoot = null;
						int prominentRootChildSize = 0;

						for (INode<IGuiCondition, ConditionLink> node : roots)
						{
							children.clear();
							node.fetchAllChildren(children);

							if (prominentRoot == null || children.size() > prominentRootChildSize)
							{
								prominentRoot = node;
								prominentRootChildSize = children.size();
							}
						}

						if (prominentRoot != root && prominentRoot != null)
						{
							GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().setProminentRoot(prominentRoot.getNodeId());
						}
					}
				}
			}

			@Override
			public void onClickNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node)
			{
				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(node.getData().getVariables(), node.getData().getName());

				GuiLayerEditor.this.selectedConditionTitle
						.setText(new Text(
								new TextComponentTranslation(
										TextFormatting.BOLD + "Variables: " + TextFormatting.RESET + "Condition " + String.valueOf(node.getNodeId())),
								1.0F));

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = null;
			}

			@Override
			public void onAddNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node, boolean oldNode)
			{
				if (!oldNode)
				{
					GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().put(node.getNodeId(), node);
				}
			}

			@Override
			public void onMoveNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node, Pos2D pos)
			{
				node.getData().setGuiPos(pos);
			}

			@Override
			public void onRemoveNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node)
			{
				GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().remove(node.getNodeId());

				if (node == GuiLayerEditor.this.currentSelectedNode)
				{
					GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
					GuiLayerEditor.this.varDisplay.reset();

					GuiLayerEditor.this.selectedConditionTitle
							.setText(new Text(
									new TextComponentTranslation(
											TextFormatting.BOLD + "Variables: "),
									1.0F));
				}
			}

			@Override
			public void onMovePane(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiLayerEditor.this.currentSelectedLayer.setConditionGuiPos(pos);
			}
		});

		this.varDisplay = new GuiVarDisplay(Dim2D.build().width(174).y(155).x(5).flush())
		{
			@Override
			public void updateVariableData()
			{
				super.updateVariableData();

				if (GuiLayerEditor.this.currentSelectedLayerNode != null)
				{
					GuiButtonVanilla button = GuiLayerEditor.this.layerTree.getButtonFromNode(GuiLayerEditor.this.currentSelectedLayerNode);

					button.getInner().displayString = GuiLayerEditor.this.currentSelectedLayerNode.getData().getOptions().getDisplayNameVar().getData();

					button.dim().mod().width(10 + this.fontRenderer.getStringWidth(button.getInner().displayString)).flush();
				}
			}
		};

		this.varDisplayScrollDecorator = new GuiScrollable(this.varDisplay, Dim2D.build().width(192).height(this.height - 160).flush());

		this.selectedLayerTitle = new GuiText(Dim2D.build().x(12).y(10).flush(),
				new Text(new TextComponentTranslation(TextFormatting.BOLD + "Conditions: "), 1.0F));

		this.selectedConditionTitle = new GuiText(Dim2D.build().x(12).y(131).flush(),
				new Text(new TextComponentTranslation(TextFormatting.BOLD + "Variables: "), 1.0F));

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		GuiText layersTitle = new GuiText(Dim2D.build().centerX(true).x(200 + ((this.width - 200) / 2) - 20).y(20).flush(),
				new Text(new TextComponentTranslation(TextFormatting.BOLD + "Layers:"), 1.0F));

		this.addChildren(this.layerTree, this.conditionTree, this.conditionWindow,
				this.varDisplayScrollDecorator, this.saveButton, this.closeButton, this.selectedLayerTitle, this.selectedConditionTitle, layersTitle,
				this.layerTab, this.postGenTab, this.dropdown);

		this.layerTree.reset(this.blueprint.getData().getTreeGuiPos() == null ?
				Pos2D.flush((this.layerTree.dim().width() / 2) - this.fontRenderer
								.getStringWidth(this.blueprint.getData().getScheduleLayerTree().get(0).getData().getOptions().getDisplayNameVar().getData()) / 2,
						(this.layerTree.dim().height() / 2) - 10) :
				this.blueprint.getData().getTreeGuiPos());

		List<Runnable> calls = Lists.newArrayList();

		for (Map.Entry<Integer, INode<IScheduleLayer, LayerLink>> e : this.blueprint.getData().getScheduleLayerTree().getInternalMap().entrySet())
		{
			INode<IScheduleLayer, LayerLink> node = e.getValue();

			// To prevent concurrent modification exception
			calls.add(() -> this.layerTree.addNode(node, node.getData().getGuiPos(), true));
		}

		calls.forEach(Runnable::run);
		calls.clear();
	}

	@Override
	public void draw()
	{
		this.conditionTree.setVisible(this.currentSelectedLayer != null);
		this.conditionTree.setEnabled(this.currentSelectedLayer != null);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.blueprint, BlueprintData.EXTENSION));
		}
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		this.varDisplay.updateVariableData();
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException
	{
		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			if (this.conditionTree.isLinkingNodes())
			{
				this.conditionTree.setLinkingNodes(false);
				return;
			}

			if (this.layerTree.isLinkingNodes())
			{
				this.layerTree.setLinkingNodes(false);
				return;
			}
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public GuiDropdownList getDropdown()
	{
		return this.dropdown;
	}
}
