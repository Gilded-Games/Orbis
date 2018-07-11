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
import com.gildedgames.orbis_api.client.gui.data.DropdownElementWithData;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.*;
import com.gildedgames.orbis_api.client.gui.util.decorators.GuiScrollable;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.client.rect.RectModifier;
import com.gildedgames.orbis_api.core.tree.ConditionLink;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.LayerLink;
import com.gildedgames.orbis_api.core.tree.NodeMultiParented;
import com.gildedgames.orbis_api.core.variables.*;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionCheckBlueprintVariable;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionPercentage;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionRatio;
import com.gildedgames.orbis_api.core.variables.conditions.IGuiCondition;
import com.gildedgames.orbis_api.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis_api.core.variables.post_resolve_actions.IPostResolveAction;
import com.gildedgames.orbis_api.core.variables.post_resolve_actions.PostResolveActionMutateBlueprintVariable;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintVariable;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.data.schedules.ScheduleLayer;
import com.gildedgames.orbis_api.util.InputHelper;
import com.gildedgames.orbis_api.util.mc.NBT;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiLayerEditor extends GuiFrame implements IDropdownHolder
{

	private static final ResourceLocation LAYERS_ICON = OrbisCore.getResource("blueprint_gui/layers_icon.png");

	private static final ResourceLocation POST_GEN_ICON = OrbisCore.getResource("blueprint_gui/post_gen_icon.png");

	private GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> layerTree;

	private GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> conditionTree;

	private GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> postResolveActionTree;

	private GuiTree<BlueprintVariable, NBT, GuiButtonVanilla> blueprintVariablesTree;

	private GuiSelectableTree subTreeViewer;

	private GuiVariablesHeader variablesHeader;

	private GuiVarDisplay varDisplay;

	private GuiScrollable varDisplayScrollDecorator;

	private IScheduleLayer currentSelectedLayer;

	private Blueprint blueprint;

	private GuiButtonVanilla saveButton, closeButton, blueprintVariablesButton;

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
		this.setDrawDefaultBackground(true);

		this.blueprint = blueprint;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.subTreeViewer = new GuiSelectableTree(Dim2D.build().flush());
		this.variablesHeader = new GuiVariablesHeader(Dim2D.build().flush());

		this.subTreeViewer.setTitle(new TextComponentTranslation("orbis.gui.selected", ""));

		this.variablesHeader.dim().add(new RectModifier("yOffsetFromTreeViewer", this.subTreeViewer, RectModifier.ModifierType.HEIGHT.getModification(),
				RectModifier.ModifierType.Y));

		this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));

		this.dropdown = new GuiDropdownList<DropdownElementWithData<Supplier<IGuiCondition>>>(Dim2D.build().width(60).flush());

		this.dropdown.setZOrder(Integer.MAX_VALUE);

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

		GuiFrameDummy buttons = new GuiFrameDummy(Dim2D.build().width(220).height(20).centerX(true).x(200 + ((this.width - 200) / 2)).y(20).flush());

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).flush());

		this.saveButton.getInner().displayString = I18n.format("orbis.gui.save_as");

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).x(55).flush());

		this.closeButton.getInner().displayString = I18n.format("orbis.gui.close");

		this.blueprintVariablesButton = new GuiButtonVanilla(
				Dim2D.build().width(110).height(20).x(110).flush());

		this.blueprintVariablesButton.getInner().displayString = I18n.format("orbis.gui.blueprint_variables");

		buttons.addChildren(this.saveButton, this.closeButton, this.blueprintVariablesButton);

		this.layerTree = new GuiTree<>(Dim2D.build().width(this.width - 200).height(this.height - 40).x(200).y(40).flush(), (nodeId) ->
		{
			NodeMultiParented<IScheduleLayer, LayerLink> node = new NodeMultiParented<>(
					new ScheduleLayer(I18n.format("orbis.gui.layer") + " " + String.valueOf(nodeId + 1), this.blueprint), false);

			node.setNodeId(nodeId);

			return node;
		},
				() -> Collections.singleton(new DropdownElement(new TextComponentTranslation("orbis.gui.link_child"))
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
				},
				() -> this.blueprint.getData().getScheduleLayerTree().findNextAvailableId());

		final List<INode<IGuiCondition, ConditionLink>> roots = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> visitedNodes = Lists.newArrayList();
		final List<INode<IGuiCondition, ConditionLink>> children = Lists.newArrayList();

		this.conditionTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			NodeMultiParented<IGuiCondition, ConditionLink> node = new NodeMultiParented<>(new GuiConditionPercentage(), true);

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

					button.getInner().displayString = "C" + String.valueOf(n.getNodeId());

					return button;
				},
				() -> this.currentSelectedLayer.getConditionNodeTree().findNextAvailableId());

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
				Collections::emptyList,
				(l) -> "",
				(n) -> false,
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
					new BlueprintVariable(new GuiVarInteger("orbis.gui.value"), "V" + String.valueOf(nodeId)), true,
					false);

			node.setNodeId(nodeId);

			return node;
		},
				Collections::emptyList,
				(l) -> "",
				(n) -> false,
				(n) ->
				{
					String name = n.getData().getUniqueNameVar().getData();

					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(10 + this.fontRenderer.getStringWidth(name)).height(20).flush());

					button.getInner().displayString = name;

					return button;
				},
				() -> this.blueprint.getData().getVariableTree().findNextAvailableId());

		this.conditionTree.setZOrder(1);
		this.postResolveActionTree.setZOrder(1);
		this.blueprintVariablesTree.setZOrder(1);

		this.layerTree.setInputDisabledWhenNotHovered(true);
		this.conditionTree.setInputDisabledWhenNotHovered(true);
		this.postResolveActionTree.setInputDisabledWhenNotHovered(true);
		this.blueprintVariablesTree.setInputDisabledWhenNotHovered(true);

		this.conditionTree.setVisible(false);
		this.conditionTree.setEnabled(false);

		this.blueprintVariablesTree.setVisible(false);
		this.blueprintVariablesTree.setEnabled(false);

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

				GuiLayerEditor.this.currentSelectedNode = node;
				GuiLayerEditor.this.currentSelectedLayerNode = node;
				GuiLayerEditor.this.currentSelectedConditionNode = null;
				GuiLayerEditor.this.currentSelectedVariableNode = null;
				GuiLayerEditor.this.currentSelectedPostResolveActionNode = null;

				GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", String
						.valueOf(node.getData().getOptions().getDisplayNameVar().getData())));
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

					GuiLayerEditor.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));
				}
			}

			@Override
			public void onMovePane(GuiTree<IScheduleLayer, LayerLink, GuiButtonVanilla> tree, Pos2D pos)
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
					GuiButtonVanilla button = GuiLayerEditor.this.layerTree.getButtonFromNode(GuiLayerEditor.this.currentSelectedLayerNode);

					if (button != null)
					{
						button.getInner().displayString = GuiLayerEditor.this.currentSelectedLayerNode.getData().getOptions().getDisplayNameVar().getData();

						button.dim().mod().width(10 + this.fontRenderer.getStringWidth(button.getInner().displayString)).flush();
					}
				}

				if (GuiLayerEditor.this.currentSelectedVariableNode != null)
				{
					GuiButtonVanilla button = GuiLayerEditor.this.blueprintVariablesTree.getButtonFromNode(GuiLayerEditor.this.currentSelectedVariableNode);

					if (button != null)
					{
						button.getInner().displayString = GuiLayerEditor.this.currentSelectedVariableNode.getData().getUniqueNameVar().getData();

						button.dim().mod().width(10 + this.fontRenderer.getStringWidth(button.getInner().displayString)).flush();
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

		this.conditionsDropdown.setEnabled(false);
		this.conditionsDropdown.setVisible(false);

		this.blueprintVariableDropdown.setEnabled(false);
		this.blueprintVariableDropdown.setVisible(false);

		this.addChildren(this.layerTree,
				this.varDisplayScrollDecorator, buttons,
				layersTitle,
				this.layerTab, this.postGenTab, this.dropdown, this.variablesHeader,
				this.subTreeViewer);

		this.layerTree.reset(this.blueprint.getData().getScheduleTreeGuiPos() == null ?
				Pos2D.flush((this.layerTree.dim().width() / 2) - this.fontRenderer
								.getStringWidth(this.blueprint.getData().getScheduleLayerTree().get(0).getData().getOptions().getDisplayNameVar().getData()) / 2,
						(this.layerTree.dim().height() / 2) - 10) :
				this.blueprint.getData().getScheduleTreeGuiPos());

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
	public void draw()
	{

	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHoveredAndTopElement(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame() == null ? null : this.getPrevFrame().getActualScreen());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHoveredAndTopElement(this.saveButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.blueprint, BlueprintData.EXTENSION));
		}

		if (InputHelper.isHoveredAndTopElement(this.blueprintVariablesButton) && mouseButton == 0)
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

	private static class GuiSelectableTree extends GuiFrame
	{
		private static final ResourceLocation TREE_WINDOW = OrbisCore.getResource("layer_gui/tree_window.png");

		private static final ResourceLocation TREE_WINDOW_EXTENDED = OrbisCore.getResource("layer_gui/tree_window_extended.png");

		private GuiTexture window;

		private GuiText title;

		private List<Pair<ITextComponent, GuiTree>> managedTrees = Lists.newArrayList();

		private GuiDropdown<DropdownElementWithData<GuiTree>> dropdown;

		private GuiTree currentTree;

		public GuiSelectableTree(Rect rect)
		{
			super(rect);
		}

		public void setTitle(ITextComponent text)
		{
			if (this.title == null)
			{
				this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
						new Text(text, 1.0F));
			}
			else
			{
				this.title.setText(new Text(text, 1.0F));
			}
		}

		public void setTreeNoDropdown(GuiTree tree, ITextComponent text)
		{
			this.managedTrees.clear();
			this.clearChildren();

			this.init();

			this.window.setResourceLocation(TREE_WINDOW, 200, 121);
			this.title.setText(new Text(text, 1.0F));

			tree.dim().mod().width(184).height(86).x(8).y(27).flush();

			tree.setEnabled(true);
			tree.setVisible(true);

			this.addChildren(tree);
		}

		public void setTrees(ITextComponent text, Pair<ITextComponent, GuiTree>... trees)
		{
			this.managedTrees.clear();

			this.managedTrees.addAll(Arrays.asList(trees));

			this.clearChildren();

			this.init();

			this.window.setResourceLocation(TREE_WINDOW_EXTENDED, 200, 141);
			this.title.setText(new Text(text, 1.0F));

			this.dropdown.getList().getElements().clear();

			for (Pair<ITextComponent, GuiTree> pair : this.managedTrees)
			{
				ITextComponent t = pair.getLeft();
				GuiTree tree = pair.getRight();

				this.dropdown.getList().addDropdownElements(new DropdownElementWithData<>(t, tree));
			}

			if (this.dropdown.getList().getElements().size() >= 1)
			{
				this.dropdown.setChosenElement(this.dropdown.getList().getElements().get(0));
			}

			this.managedTrees.forEach((p) ->
			{
				p.getRight().dim().mod().width(184).height(86).x(8).y(27 + 19).flush();

				p.getRight().setEnabled(false);
				p.getRight().setVisible(false);

				this.addChildren(p.getRight());
			});

			if (!this.managedTrees.isEmpty())
			{
				this.currentTree = this.managedTrees.get(0).getRight();

				this.currentTree.setEnabled(true);
				this.currentTree.setVisible(true);
			}
		}

		@Override
		public void init()
		{
			if (this.window == null)
			{
				this.window = new GuiTexture(Dim2D.build().width(200).height(121).flush(), TREE_WINDOW);
			}

			if (this.title == null)
			{
				this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
						new Text(new TextComponentTranslation("orbis.gui.conditions", ""), 1.0F));
			}

			if (!this.dim().containsModifier("windowArea", this.window))
			{
				this.dim().add(new RectModifier("windowArea", this.window, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
			}

			this.addChildren(this.window, this.title);

			if (this.dropdown == null)
			{
				this.dropdown = new GuiDropdown<>(Dim2D.build().x(7).y(25).width(186).flush(), (e) -> {
					if (this.currentTree != null)
					{
						this.currentTree.setEnabled(false);
						this.currentTree.setVisible(false);
					}

					this.currentTree = e.getData();

					this.currentTree.setEnabled(true);
					this.currentTree.setVisible(true);
				});
			}

			if (this.managedTrees.size() >= 2)
			{
				this.addChildren(this.dropdown);
			}
		}
	}

	private static class GuiVariablesHeader extends GuiFrame
	{
		private static final ResourceLocation VARIABLE_HEADER = OrbisCore.getResource("layer_gui/variable_header.png");

		private static final ResourceLocation VARIABLE_HEADER_EXTENDED = OrbisCore.getResource("layer_gui/variable_header_extended.png");

		private GuiTexture window;

		private GuiText title, type;

		private GuiDropdown dropdown;

		public GuiVariablesHeader(Rect rect)
		{
			super(rect);
		}

		public void setTitle(ITextComponent text)
		{
			this.setTitleAndDropdown(text, null);
		}

		public void setTitleAndDropdown(ITextComponent text, @Nullable GuiDropdown dropdown)
		{
			this.clearChildren();

			this.init();

			this.window.setResourceLocation(dropdown == null ? VARIABLE_HEADER : VARIABLE_HEADER_EXTENDED, 200, dropdown == null ? 29 : 49);
			this.title.setText(new Text(text, 1.0F));
			this.type.setVisible(dropdown != null);

			if (dropdown != null)
			{
				this.dropdown = dropdown;

				this.dropdown.setEnabled(true);
				this.dropdown.setVisible(true);

				this.dropdown.dim().mod().x(40).y(25).flush();

				this.addChildren(this.dropdown);
			}
		}

		@Override
		public void init()
		{
			if (this.window == null)
			{
				this.window = new GuiTexture(Dim2D.build().width(200).height(29).flush(), VARIABLE_HEADER);
			}

			if (this.title == null)
			{
				this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
						new Text(new TextComponentString(""), 1.0F));
			}

			if (this.type == null)
			{
				this.type = new GuiText(Dim2D.build().x(8).y(30).flush(),
						new Text(new TextComponentTranslation("orbis.gui.type"), 1.0F));

				this.type.setVisible(false);
			}

			if (!this.dim().containsModifier("windowArea", this.window))
			{
				this.dim().add(new RectModifier("windowArea", this.window, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
			}

			this.addChildren(this.window, this.title, this.type);
		}
	}
}
