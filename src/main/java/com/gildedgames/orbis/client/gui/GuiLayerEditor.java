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
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElementWithData;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.util.*;
import com.gildedgames.orbis.lib.client.gui.util.decorators.GuiScrollable;
import com.gildedgames.orbis.lib.client.gui.util.events.MouseInputDisabledWhenNotHovered;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.client.rect.RectModifier;
import com.gildedgames.orbis.lib.core.tree.ConditionLink;
import com.gildedgames.orbis.lib.core.tree.INode;
import com.gildedgames.orbis.lib.core.tree.LayerLink;
import com.gildedgames.orbis.lib.core.tree.NodeMultiParented;
import com.gildedgames.orbis.lib.core.variables.*;
import com.gildedgames.orbis.lib.core.variables.conditions.GuiConditionCheckBlueprintVariable;
import com.gildedgames.orbis.lib.core.variables.conditions.GuiConditionPercentage;
import com.gildedgames.orbis.lib.core.variables.conditions.GuiConditionRatio;
import com.gildedgames.orbis.lib.core.variables.conditions.IGuiCondition;
import com.gildedgames.orbis.lib.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis.lib.core.variables.post_resolve_actions.IPostResolveAction;
import com.gildedgames.orbis.lib.core.variables.post_resolve_actions.PostResolveActionMutateBlueprintVariable;
import com.gildedgames.orbis.lib.data.IDataUser;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintVariable;
import com.gildedgames.orbis.lib.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.lib.data.schedules.ScheduleLayer;
import com.gildedgames.orbis.lib.util.InputHelper;
import com.gildedgames.orbis.lib.util.mc.NBT;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiLayerEditor extends GuiViewer implements IDropdownHolder
{

	private static final ResourceLocation LAYERS_ICON = OrbisCore.getResource("blueprint_gui/layers_icon.png");

	private static final ResourceLocation POST_GEN_ICON = OrbisCore.getResource("blueprint_gui/post_gen_icon.png");

	private GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> layerTree;

	private GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> conditionTree;

	private GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> postResolveActionTree;

	private GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> blueprintVariablesTree;

	private GuiSelectableTree subTreeViewer;

	private GuiVariablesHeader variablesHeader;

	private GuiVarDisplay varDisplay;

	private GuiScrollable varDisplayScrollDecorator;

	private IScheduleLayer currentSelectedLayer;

	private Blueprint blueprint;

	private GuiButtonVanilla saveButton, closeButton, blueprintVariablesButton, metadataButton;

	private GuiTab layerTab, postGenTab;

	private INode currentSelectedNode;

	private INode<IScheduleLayer, LayerLink> currentSelectedLayerNode;

	private INode<IGuiCondition, ConditionLink> currentSelectedConditionNode;

	private INode<BlueprintVariable, NBT> currentSelectedVariableNode;

	private INode<IPostResolveAction, NBT> currentSelectedPostResolveActionNode;

	private GuiDropdownList dropdown;

	private GuiDropdown<DropdownElementWithData<Supplier<IGuiCondition>>> conditionsDropdown;

	private GuiDropdown<DropdownElementWithData<Supplier<IPostResolveAction>>> postResolveActionDropdown;

	private GuiDropdown<DropdownElementWithData<Function<BlueprintVariable, BlueprintVariable>>> blueprintVariableDropdown;

	public GuiLayerEditor(Blueprint blueprint)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(true);

		this.blueprint = blueprint;
	}

	@Override
	public void build(IGuiContext context)
	{
		this.subTreeViewer = new GuiSelectableTree(Dim2D.build().flush());
		this.variablesHeader = new GuiVariablesHeader(Dim2D.build().flush());

		this.subTreeViewer.setTitle(new TextComponentTranslation("orbis.gui.selected", ""));

		this.variablesHeader.dim().add(new RectModifier("yOffsetFromTreeViewer", this.subTreeViewer, RectModifier.ModifierType.HEIGHT.getModification(),
				RectModifier.ModifierType.Y));

		this.variablesHeader.build(this);

		this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));

		this.dropdown = new GuiDropdownList<DropdownElementWithData<Supplier<IGuiCondition>>>(Dim2D.build().width(60).flush());

		this.dropdown.build(this);

		this.dropdown.state().setZOrder(Integer.MAX_VALUE);

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

		GuiElement buttons = new GuiElement(Dim2D.build().width(255).height(20).centerX(true).x(200 + ((this.width - 200) / 2)).y(20).flush(), false);

		buttons.build(this);

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).flush());

		this.saveButton.getInner().displayString = I18n.format("orbis.gui.save_as");

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).x(55).flush());

		this.closeButton.getInner().displayString = I18n.format("orbis.gui.close");

		this.blueprintVariablesButton = new GuiButtonVanilla(
				Dim2D.build().width(70).height(20).x(110).flush());

		this.blueprintVariablesButton.getInner().displayString = I18n.format("orbis.gui.variables_title");

		this.metadataButton = new GuiButtonVanilla(
				Dim2D.build().width(70).height(20).x(185).flush());

		this.metadataButton.getInner().displayString = I18n.format("orbis.gui.metadata");

		final List<INode<IScheduleLayer, LayerLink>> layerRoots = Lists.newArrayList();
		final List<INode<IScheduleLayer, LayerLink>> layerVisitedNodes = Lists.newArrayList();

		buttons.context().addChildren(this.saveButton, this.closeButton, this.blueprintVariablesButton, this.metadataButton);

		this.layerTree = new GuiTree<>(Dim2D.build().width(this.width - 200).height(this.height - 40).x(200).y(40).flush(), (nodeId) ->
		{
			NodeMultiParented<IScheduleLayer, LayerLink> node = new NodeMultiParented<>(
					new ScheduleLayer(I18n.format("orbis.gui.layer") + " " + String.valueOf(nodeId + 1), this.blueprint), false);

			node.setNodeId(nodeId);

			return node;
		},
				(node) ->
				{
					List<IDropdownElement> elements = Lists.newArrayList();

					elements.add(new DropdownElement(new TextComponentTranslation("orbis.gui.link_child"))
					{
						@Override
						public void onClick(final GuiDropdownList list, final EntityPlayer player)
						{
							GuiLayerEditor.this.layerTree.startLinking(LayerLink.DEFAULT);
						}
					});

					if (!node.getChildrenIds().isEmpty())
					{
						elements.add(new DropdownElement(new TextComponentTranslation("orbis.gui.unlink_child"), () -> {
							GuiDropdownList list = new GuiDropdownList(Dim2D.flush());

							for (INode<IScheduleLayer, LayerLink> child : node.getTree().get(node.getChildrenIds()))
							{
								list.addDropdownElements(
										new DropdownElement(new TextComponentString(child.getData().getOptions().getDisplayNameVar().getData()))
										{
											@Override
											public void onClick(final GuiDropdownList list, final EntityPlayer player)
											{
												node.removeChild(child.getNodeId());
											}
										});
							}

							return list;
						}));
					}

					if (!node.getParentsIds().isEmpty())
					{
						elements.add(new DropdownElement(new TextComponentTranslation("orbis.gui.unlink_parent"), () -> {
							GuiDropdownList list = new GuiDropdownList(Dim2D.flush());

							for (INode<IScheduleLayer, LayerLink> parent : node.getTree().get(node.getParentsIds()))
							{
								list.addDropdownElements(
										new DropdownElement(new TextComponentString(parent.getData().getOptions().getDisplayNameVar().getData()))
										{
											@Override
											public void onClick(final GuiDropdownList list, final EntityPlayer player)
											{
												parent.removeChild(node.getNodeId());
											}
										});
							}

							return list;
						}));
					}

					return elements;
				},
				(l) -> "",
				(n) ->
				{
					layerRoots.clear();
					layerVisitedNodes.clear();

					n.fetchRoots(layerRoots, layerVisitedNodes);

					return layerRoots.contains(n.getTree().getRootNode()) || n.getNodeId() == n.getTree().getRootNodeId();
				},
				(n) ->
				{
					GuiLayerButton button = new GuiLayerButton(this,
							Dim2D.build().width(25 + this.fontRenderer.getStringWidth(n.getData().getOptions().getDisplayNameVar().getData())).height(20)
									.flush(), n);

					button.getInner().displayString = "    " + n.getData().getOptions().getDisplayNameVar().getData();

					return button;
				},
				() -> this.blueprint.getData().getScheduleLayerTree().findNextAvailableId());

		this.layerTree.setCanDeleteNode((node) -> node.getTree().getRootNodeId() != node.getNodeId());

		final List<INode<IGuiCondition, ConditionLink>> roots = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> visitedNodes = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> children = Lists.newArrayList();

		this.conditionTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			NodeMultiParented<IGuiCondition, ConditionLink> node = new NodeMultiParented<>(new GuiConditionPercentage(), true);

			node.setNodeId(nodeId);

			return node;
		},
				(node) ->
				{
					List<IDropdownElement> elements = Lists.newArrayList();

					elements.add(new DropdownElement(new TextComponentString("And..."))
					{
						@Override
						public void onClick(final GuiDropdownList list, final EntityPlayer player)
						{
							GuiLayerEditor.this.conditionTree.startLinking(ConditionLink.AND);
						}
					});

					elements.add(new DropdownElement(new TextComponentString("Or..."))
					{
						@Override
						public void onClick(final GuiDropdownList list, final EntityPlayer player)
						{
							GuiLayerEditor.this.conditionTree.startLinking(ConditionLink.OR);
						}
					});

					if (!node.getChildrenIds().isEmpty())
					{
						elements.add(new DropdownElement(new TextComponentTranslation("orbis.gui.unlink_child"), () -> {
							GuiDropdownList list = new GuiDropdownList(Dim2D.flush());

							for (INode<IGuiCondition, ConditionLink> child : node.getTree().get(node.getChildrenIds()))
							{
								list.addDropdownElements(
										new DropdownElement(new TextComponentString("C" + String.valueOf(child.getNodeId())))
										{
											@Override
											public void onClick(final GuiDropdownList list, final EntityPlayer player)
											{
												node.removeChild(child.getNodeId());
											}
										});
							}

							return list;
						}));
					}

					if (!node.getParentsIds().isEmpty())
					{
						elements.add(new DropdownElement(new TextComponentTranslation("orbis.gui.unlink_parent"), () -> {
							GuiDropdownList list = new GuiDropdownList(Dim2D.flush());

							for (INode<IGuiCondition, ConditionLink> parent : node.getTree().get(node.getParentsIds()))
							{
								list.addDropdownElements(
										new DropdownElement(new TextComponentString("C" + String.valueOf(parent.getNodeId())))
										{
											@Override
											public void onClick(final GuiDropdownList list, final EntityPlayer player)
											{
												parent.removeChild(node.getNodeId());
											}
										});
							}

							return list;
						}));
					}

					return elements;
				},
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

					return !((n.getParentsIds().isEmpty() || (GuiLayerEditor.this.currentSelectedLayer != null && !roots
							.contains(GuiLayerEditor.this.currentSelectedLayer
									.getConditionNodeTree().getRootNode()))) && n != GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree()
							.getRootNode());
				},
				(n) ->
				{
					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(20).height(20).flush());

					button.getInner().displayString = "C" + String.valueOf(n.getNodeId());

					return button;
				},
				() -> this.currentSelectedLayer.getConditionNodeTree().findNextAvailableId());

		this.conditionTree.setCanDeleteNode((node) -> node.getTree().size() == 1 || node.getTree().getRootNodeId() != node.getNodeId());

		this.postResolveActionTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			PostResolveActionMutateBlueprintVariable action = new PostResolveActionMutateBlueprintVariable();

			action.setUsedData(GuiLayerEditor.this.blueprint.getData().getVariableTree());

			NodeMultiParented<IPostResolveAction, NBT> node = new NodeMultiParented<>(
					action, true,
					false);

			node.setNodeId(nodeId);

			return node;
		},
				(node) -> Collections.emptyList(),
				(l) -> "",
				(n) -> true,
				(n) ->
				{
					String name = "A" + String.valueOf(n.getNodeId());

					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(10 + this.fontRenderer.getStringWidth(name)).height(20).flush());

					button.getInner().displayString = name;

					return button;
				},
				() -> this.currentSelectedLayer.getPostResolveActionNodeTree().findNextAvailableId());

		this.blueprintVariablesTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			NodeMultiParented<BlueprintVariable, NBT> node = new NodeMultiParented<>(
					new BlueprintVariable<>(new GuiVarInteger("orbis.gui.value"), "V" + String.valueOf(nodeId)), true,
					false);

			node.setNodeId(nodeId);

			return node;
		},
				(node) -> Collections.emptyList(),
				(l) -> "",
				(n) -> true,
				(n) ->
				{
					String name = n.getData().getUniqueNameVar().getData();

					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(10 + this.fontRenderer.getStringWidth(name)).height(20).flush());

					button.getInner().displayString = name;

					return button;
				},
				() -> this.blueprint.getData().getVariableTree().findNextAvailableId());

		this.conditionTree.build(this);
		this.postResolveActionTree.build(this);
		this.blueprintVariablesTree.build(this);

		this.conditionTree.state().setZOrder(1);
		this.postResolveActionTree.state().setZOrder(1);
		this.blueprintVariablesTree.state().setZOrder(1);

		this.layerTree.state().addEvent(new MouseInputDisabledWhenNotHovered());
		this.conditionTree.state().addEvent(new MouseInputDisabledWhenNotHovered());
		this.postResolveActionTree.state().addEvent(new MouseInputDisabledWhenNotHovered());
		this.blueprintVariablesTree.state().addEvent(new MouseInputDisabledWhenNotHovered());

		this.conditionTree.state().setVisible(false);
		this.conditionTree.state().setEnabled(false);

		this.blueprintVariablesTree.state().setVisible(false);
		this.blueprintVariablesTree.state().setEnabled(false);

		this.layerTree.listen(new IGuiTreeListener<IScheduleLayer, LayerLink, GuiLayerButton>()
		{
			@Override
			public void onLinkNodes(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, INode<IScheduleLayer, LayerLink> n1,
					INode<IScheduleLayer, LayerLink> n2,
					LayerLink layerLink)
			{

			}

			@Override
			public void onClickNode(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, INode<IScheduleLayer, LayerLink> node)
			{
				GuiLayerEditor.this.currentSelectedLayer = node.getData();

				GuiLayerEditor.this.subTreeViewer.setTrees(
						new TextComponentTranslation("orbis.gui.selected", GuiLayerEditor.this.currentSelectedLayer.getOptions().getDisplayNameVar().getData()),
						Pair.of(new TextComponentTranslation("orbis.gui.conditions"), GuiLayerEditor.this.conditionTree),
						Pair.of(new TextComponentTranslation("orbis.gui.post_resolve_actions"), GuiLayerEditor.this.postResolveActionTree));

				GuiLayerEditor.this.conditionTree.reset(node.getData().getConditionGuiPos());

				for (INode<IGuiCondition, ConditionLink> n : node.getData().getConditionNodeTree().getNodes())
				{
					GuiLayerEditor.this.conditionTree.addNode(n, n.getData().getGuiPos(), true);
				}

				GuiLayerEditor.this.postResolveActionTree.reset(node.getData().getPostResolveActionGuiPos());

				for (INode<IPostResolveAction, NBT> n : node.getData().getPostResolveActionNodeTree().getNodes())
				{
					GuiLayerEditor.this.postResolveActionTree.addNode(n, n.getData().getGuiPos(), true);
				}

				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(node.getData().getOptions());

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

				if (currentSelectedLayerNode != null)
				{
					GuiLayerButton button = tree.getButtonFromNode(GuiLayerEditor.this.currentSelectedLayerNode);

					if (button != null)
					{
						button.setSelected(false);
					}
				}
				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = node;
				GuiLayerEditor.this.currentSelectedConditionNode = null;
				GuiLayerEditor.this.currentSelectedVariableNode = null;
				GuiLayerEditor.this.currentSelectedPostResolveActionNode = null;
				tree.getButtonFromNode(currentSelectedLayerNode).setSelected(true);
				GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", String
						.valueOf(node.getData().getOptions().getDisplayNameVar().getData())));
			}

			@Override
			public void onAddNode(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, INode<IScheduleLayer, LayerLink> node, boolean oldNode)
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
			public void onMoveNode(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, INode<IScheduleLayer, LayerLink> node, Pos2D pos)
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
			public void onRemoveNode(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, INode<IScheduleLayer, LayerLink> node)
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

					GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));
				}
			}

			@Override
			public void onMovePane(GuiTree<IScheduleLayer, LayerLink, GuiLayerButton> tree, Pos2D pos)
			{
				GuiLayerEditor.this.blueprint.getData().setScheduleTreeGuiPos(pos);
			}
		});

		this.conditionTree.listen(new IGuiTreeListener<IGuiCondition, ConditionLink, GuiButtonVanilla>()
		{

			@Override
			public void onLinkNodes(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> n1,
					INode<IGuiCondition, ConditionLink> n2,
					ConditionLink conditionLink)
			{

				INode<IGuiCondition, ConditionLink> root = GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().getRootNode();

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
							GuiLayerEditor.this.currentSelectedLayer.getConditionNodeTree().setRootNode(prominentRoot.getNodeId());
						}
					}
				}
			}

			@Override
			public void onClickNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node)
			{
				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(node.getData());

				GuiLayerEditor.this.variablesHeader.setTitleAndDropdown(new TextComponentTranslation("orbis.gui.variables",
						I18n.format("orbis.gui.condition") + " " + String.valueOf(node.getNodeId())), GuiLayerEditor.this.conditionsDropdown);

				DropdownElementWithData<Supplier<IGuiCondition>> element = new DropdownElementWithData<>(new TextComponentTranslation(node.getData().getName()),
						null);

				GuiLayerEditor.this.conditionsDropdown.setChosenElement(element);

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = null;
				GuiLayerEditor.this.currentSelectedConditionNode = node;
				GuiLayerEditor.this.currentSelectedVariableNode = null;
				GuiLayerEditor.this.currentSelectedPostResolveActionNode = null;
			}

			@Override
			public void onAddNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node, boolean oldNode)
			{
				if (!oldNode)
				{
					if (node.getData() instanceof IDataUser)
					{
						IDataUser dataUser = (IDataUser) node.getData();

						if (dataUser.getDataIdentifier().equals("blueprintVariables"))
						{
							dataUser.setUsedData(GuiLayerEditor.this.blueprint.getData().getVariableTree());
						}
					}

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

					GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));
				}
			}

			@Override
			public void onMovePane(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiLayerEditor.this.currentSelectedLayer.setConditionGuiPos(pos);
			}
		});

		this.postResolveActionTree.listen(new IGuiTreeListener<IPostResolveAction, NBT, GuiButtonVanilla>()
		{

			@Override
			public void onLinkNodes(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, INode<IPostResolveAction, NBT> n1,
					INode<IPostResolveAction, NBT> n2,
					NBT conditionLink)
			{

			}

			@Override
			public void onClickNode(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, INode<IPostResolveAction, NBT> node)
			{
				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(node.getData());

				GuiLayerEditor.this.variablesHeader
						.setTitleAndDropdown(new TextComponentTranslation("orbis.gui.variables",
										I18n.format("orbis.gui.post_resolve_action") + " " + String.valueOf(node.getNodeId())),
								GuiLayerEditor.this.postResolveActionDropdown);

				DropdownElementWithData<Supplier<IPostResolveAction>> element = new DropdownElementWithData<>(
						new TextComponentTranslation(node.getData().getName()),
						null);

				GuiLayerEditor.this.postResolveActionDropdown.setChosenElement(element);

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = null;
				GuiLayerEditor.this.currentSelectedConditionNode = null;
				GuiLayerEditor.this.currentSelectedVariableNode = null;
				GuiLayerEditor.this.currentSelectedPostResolveActionNode = node;
			}

			@Override
			public void onAddNode(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, INode<IPostResolveAction, NBT> node, boolean oldNode)
			{
				if (!oldNode)
				{
					if (node.getData() instanceof IDataUser)
					{
						IDataUser dataUser = (IDataUser) node.getData();

						if (dataUser.getDataIdentifier().equals("blueprintVariables"))
						{
							dataUser.setUsedData(GuiLayerEditor.this.blueprint.getData().getVariableTree());
						}
					}

					GuiLayerEditor.this.currentSelectedLayer.getPostResolveActionNodeTree().put(node.getNodeId(), node);
				}
			}

			@Override
			public void onMoveNode(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, INode<IPostResolveAction, NBT> node, Pos2D pos)
			{
				node.getData().setGuiPos(pos);
			}

			@Override
			public void onRemoveNode(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, INode<IPostResolveAction, NBT> node)
			{
				GuiLayerEditor.this.currentSelectedLayer.getPostResolveActionNodeTree().remove(node.getNodeId());

				if (node == GuiLayerEditor.this.currentSelectedNode)
				{
					GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
					GuiLayerEditor.this.varDisplay.reset();

					GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));

					GuiLayerEditor.this.currentSelectedVariableNode = null;
				}
			}

			@Override
			public void onMovePane(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiLayerEditor.this.currentSelectedLayer.setPostResolveActionGuiPos(pos);
			}
		});

		this.blueprintVariablesTree.listen(new IGuiTreeListener<BlueprintVariable, NBT, GuiButtonVanilla>()
		{

			@Override
			public void onLinkNodes(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, INode<BlueprintVariable, NBT> n1,
					INode<BlueprintVariable, NBT> n2,
					NBT conditionLink)
			{

			}

			@Override
			public void onClickNode(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, INode<BlueprintVariable, NBT> node)
			{
				GuiLayerEditor.this.varDisplay.updateVariableData();

				GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
				GuiLayerEditor.this.varDisplay.display(node.getData());

				GuiLayerEditor.this.variablesHeader
						.setTitleAndDropdown(new TextComponentTranslation("orbis.gui.variables", node.getData().getUniqueNameVar().getData()),
								GuiLayerEditor.this.blueprintVariableDropdown);

				DropdownElementWithData<Function<BlueprintVariable, BlueprintVariable>> element = new DropdownElementWithData<>(
						new TextComponentTranslation(node.getData().getVar().getDataName()),
						null);

				GuiLayerEditor.this.blueprintVariableDropdown.setChosenElement(element);

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = null;
				GuiLayerEditor.this.currentSelectedConditionNode = null;
				GuiLayerEditor.this.currentSelectedVariableNode = node;
				GuiLayerEditor.this.currentSelectedPostResolveActionNode = null;
			}

			@Override
			public void onAddNode(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, INode<BlueprintVariable, NBT> node, boolean oldNode)
			{
				if (!oldNode)
				{
					GuiLayerEditor.this.blueprint.getData().getVariableTree().put(node.getNodeId(), node);
				}
			}

			@Override
			public void onMoveNode(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, INode<BlueprintVariable, NBT> node, Pos2D pos)
			{
				node.getData().setGuiPos(pos);
			}

			@Override
			public void onRemoveNode(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, INode<BlueprintVariable, NBT> node)
			{
				GuiLayerEditor.this.blueprint.getData().getVariableTree().remove(node.getNodeId());

				if (node == GuiLayerEditor.this.currentSelectedNode)
				{
					GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
					GuiLayerEditor.this.varDisplay.reset();

					GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));

					GuiLayerEditor.this.currentSelectedVariableNode = null;
				}
			}

			@Override
			public void onMovePane(GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiLayerEditor.this.blueprint.getData().setVariableTreeGuiPos(pos);
			}
		});

		this.varDisplay = new GuiVarDisplay(Dim2D.build().width(174).x(5).flush())
		{
			@Override
			public void updateVariableData()
			{
				super.updateVariableData();

				if (GuiLayerEditor.this.currentSelectedLayerNode != null)
				{
					GuiLayerButton button = GuiLayerEditor.this.layerTree.getButtonFromNode(GuiLayerEditor.this.currentSelectedLayerNode);

					if (button != null)
					{
						button.getInner().displayString =
								"    " + GuiLayerEditor.this.currentSelectedLayerNode.getData().getOptions().getDisplayNameVar().getData();

						button.dim().mod().width(25 + this.viewer().fontRenderer()
								.getStringWidth(GuiLayerEditor.this.currentSelectedLayerNode.getData().getOptions().getDisplayNameVar().getData())).flush();
					}
				}

				if (GuiLayerEditor.this.currentSelectedVariableNode != null)
				{
					GuiButtonVanilla button = GuiLayerEditor.this.blueprintVariablesTree.getButtonFromNode(GuiLayerEditor.this.currentSelectedVariableNode);

					if (button != null)
					{
						button.getInner().displayString = GuiLayerEditor.this.currentSelectedVariableNode.getData().getUniqueNameVar().getData();

						button.dim().mod().width(10 + this.viewer().fontRenderer().getStringWidth(button.getInner().displayString)).flush();
					}
				}

				GuiLayerEditor.this.blueprint.markDirty();
			}
		};

		this.varDisplayScrollDecorator = new GuiScrollable(this.varDisplay, Dim2D.build().width(192).flush());

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		GuiText layersTitle = new GuiText(Dim2D.build().centerX(true).x(200 + ((this.width - 200) / 2)).y(5).flush(),
				new Text(new TextComponentTranslation("orbis.gui.layers"), 1.0F));

		this.conditionsDropdown = new GuiDropdown<>(Dim2D.build().width(153).flush(), (e) ->
		{
			IGuiCondition condition = e.getData().get();

			condition.setGuiPos(GuiLayerEditor.this.currentSelectedConditionNode.getData().getGuiPos());

			GuiLayerEditor.this.currentSelectedConditionNode.setData(condition);

			GuiLayerEditor.this.varDisplay.updateVariableData();

			GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
			GuiLayerEditor.this.varDisplay.display(condition);
		},
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.percentage"), GuiConditionPercentage::new),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.ratio"), GuiConditionRatio::new),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.check_blueprint_variable"),
						() ->
						{
							GuiConditionCheckBlueprintVariable cond = new GuiConditionCheckBlueprintVariable();

							cond.setUsedData(GuiLayerEditor.this.blueprint.getData().getVariableTree());

							return cond;
						}));

		this.postResolveActionDropdown = new GuiDropdown<>(Dim2D.build().width(153).flush(), (e) ->
		{
			IPostResolveAction action = e.getData().get();

			action.setGuiPos(GuiLayerEditor.this.currentSelectedPostResolveActionNode.getData().getGuiPos());

			GuiLayerEditor.this.currentSelectedPostResolveActionNode.setData(action);

			GuiLayerEditor.this.varDisplay.updateVariableData();

			GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
			GuiLayerEditor.this.varDisplay.display(action);
		},
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.mutate_blueprint_variable"), () ->
				{
					PostResolveActionMutateBlueprintVariable action = new PostResolveActionMutateBlueprintVariable();

					action.setUsedData(GuiLayerEditor.this.blueprint.getData().getVariableTree());

					return action;
				}));

		this.blueprintVariableDropdown = new GuiDropdown<>(Dim2D.build().width(153).flush(), (e) ->
		{
			BlueprintVariable variable = e.getData().apply(GuiLayerEditor.this.currentSelectedVariableNode.getData());

			GuiLayerEditor.this.currentSelectedVariableNode.setData(variable);

			GuiLayerEditor.this.varDisplay.updateVariableData();

			GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
			GuiLayerEditor.this.varDisplay.display(variable);
		},
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.integer"),
						(var) ->
						{
							BlueprintVariable v = new BlueprintVariable(new GuiVarInteger("orbis.gui.value"), var.getUniqueNameVar().getData());

							v.setGuiPos(var.getGuiPos());

							return v;
						}),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.boolean"),
						(var) -> {
							BlueprintVariable v = new BlueprintVariable(new GuiVarBoolean("orbis.gui.value"), var.getUniqueNameVar().getData());

							v.setGuiPos(var.getGuiPos());

							return v;
						}),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.float"),
						(var) -> {
							BlueprintVariable v = new BlueprintVariable(new GuiVarFloat("orbis.gui.value"), var.getUniqueNameVar().getData());

							v.setGuiPos(var.getGuiPos());

							return v;
						}),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.double"),
						(var) -> {
							BlueprintVariable v = new BlueprintVariable(new GuiVarDouble("orbis.gui.value"), var.getUniqueNameVar().getData());

							v.setGuiPos(var.getGuiPos());

							return v;
						}),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.string"),
						(var) -> {
							BlueprintVariable v = new BlueprintVariable(new GuiVarString("orbis.gui.value"), var.getUniqueNameVar().getData());

							v.setGuiPos(var.getGuiPos());

							return v;
						}));

		this.varDisplayScrollDecorator.dim()
				.add(new RectModifier("yOffsetFromVarHeader", this.variablesHeader, (source, modifying) -> source.dim().maxY() + 5,
						RectModifier.ModifierType.Y));
		this.varDisplayScrollDecorator.dim()
				.add(new RectModifier("heightBelowVarHeader", this.variablesHeader, (source, modifying) -> this.height - source.dim().maxY() - 10,
						RectModifier.ModifierType.HEIGHT));

		this.conditionsDropdown.state().setEnabled(false);
		this.conditionsDropdown.state().setVisible(false);

		this.blueprintVariableDropdown.state().setEnabled(false);
		this.blueprintVariableDropdown.state().setVisible(false);

		this.subTreeViewer.dim().mod().width(200).height(141).flush();

		context.addChildren(this.layerTree,
				this.varDisplayScrollDecorator, buttons,
				layersTitle,
				this.layerTab, this.postGenTab, this.dropdown, this.variablesHeader,
				this.subTreeViewer);

		if (this.blueprint.getData().getScheduleLayerTree().isEmpty())
		{
			this.layerTree.reset(Pos2D.ORIGIN);
		}
		else
		{
			this.layerTree.reset(this.blueprint.getData().getScheduleTreeGuiPos() == null ?
					Pos2D.flush((this.layerTree.dim().width() / 2) - this.fontRenderer
									.getStringWidth(this.blueprint.getData().getScheduleLayerTree().get(0).getData().getOptions().getDisplayNameVar().getData()) / 2,
							(this.layerTree.dim().height() / 2) - 10) :
					this.blueprint.getData().getScheduleTreeGuiPos());
		}

		this.blueprintVariablesTree.reset(this.blueprint.getData().getVariableTreeGuiPos());

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
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.closeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPreviousViewer() == null ? null : this.getPreviousViewer().getActualScreen());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (this.saveButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.blueprint, BlueprintData.EXTENSION));
		}

		if (this.blueprintVariablesButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			GuiLayerEditor.this.subTreeViewer
					.setTreeNoDropdown(this.blueprintVariablesTree, new TextComponentTranslation("orbis.gui.blueprint_variables_title"));

			this.blueprintVariablesTree.reset(this.blueprint.getData().getVariableTreeGuiPos());

			for (INode<BlueprintVariable, NBT> n : this.blueprint.getData().getVariableTree().getNodes())
			{
				this.blueprintVariablesTree.addNode(n, n.getData().getGuiPos(), true);
			}

			this.currentSelectedLayer = null;
		}

		if (this.metadataButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			GuiLayerEditor.this.varDisplay.updateVariableData();

			GuiLayerEditor.this.varDisplayScrollDecorator.resetScroll();
			GuiLayerEditor.this.varDisplay.display(this.blueprint.getData().getBlueprintMetadata());
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

	public IScheduleLayer getCurrentSelectedLayer()
	{
		return currentSelectedLayer;
	}

	public INode getCurrentSelectedNode()
	{
		return currentSelectedNode;
	}

	public INode<IScheduleLayer, LayerLink> getCurrentSelectedLayerNode()
	{
		return currentSelectedLayerNode;
	}

	public INode<IGuiCondition, ConditionLink> getCurrentSelectedConditionNode()
	{
		return currentSelectedConditionNode;
	}

	public INode<BlueprintVariable, NBT> getCurrentSelectedVariableNode()
	{
		return currentSelectedVariableNode;
	}

	public INode<IPostResolveAction, NBT> getCurrentSelectedPostResolveActionNode()
	{
		return currentSelectedPostResolveActionNode;
	}

	@Override
	public GuiDropdownList getDropdown()
	{
		return this.dropdown;
	}
}
