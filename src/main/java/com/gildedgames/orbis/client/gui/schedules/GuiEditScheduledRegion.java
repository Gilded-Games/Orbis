package com.gildedgames.orbis.client.gui.schedules;

import com.gildedgames.orbis.client.gui.GuiSelectableTree;
import com.gildedgames.orbis.client.gui.GuiTree;
import com.gildedgames.orbis.client.gui.GuiVariablesHeader;
import com.gildedgames.orbis.client.gui.IGuiTreeListener;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketSetTriggerId;
import com.gildedgames.orbis.common.variables.post_resolve_actions.PostResolveActionApplyLootTable;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.data.DropdownElementWithData;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.*;
import com.gildedgames.orbis_api.client.gui.util.decorators.GuiScrollable;
import com.gildedgames.orbis_api.client.gui.util.events.MouseInputDisabledWhenNotHovered;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.gui.util.repeat_methods.TextureRepeatMethods;
import com.gildedgames.orbis_api.client.gui.util.repeat_methods.TextureUV;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.client.rect.RectModifier;
import com.gildedgames.orbis_api.core.tree.ConditionLink;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.NodeMultiParented;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionCheckBlueprintVariable;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionPercentage;
import com.gildedgames.orbis_api.core.variables.conditions.GuiConditionRatio;
import com.gildedgames.orbis_api.core.variables.conditions.IGuiCondition;
import com.gildedgames.orbis_api.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis_api.core.variables.post_resolve_actions.IPostResolveAction;
import com.gildedgames.orbis_api.core.variables.post_resolve_actions.PostResolveActionMutateBlueprintVariable;
import com.gildedgames.orbis_api.core.variables.post_resolve_actions.PostResolveActionSpawnEntities;
import com.gildedgames.orbis_api.data.IDataUser;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.util.mc.NBT;
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
import java.util.function.Supplier;

public class GuiEditScheduledRegion extends GuiViewer implements IDropdownHolder
{
	private static final ResourceLocation CONTAINER = OrbisCore.getResource("generic/container.png");

	private final ISchedule schedule;

	private GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> conditionTree;

	private GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> postResolveActionTree;

	private GuiSelectableTree subTreeViewer;

	private GuiVariablesHeader variablesHeader;

	private GuiVarDisplay varDisplay;

	private GuiScrollable varDisplayScrollDecorator;

	private INode currentSelectedNode;

	private INode<IGuiCondition, ConditionLink> currentSelectedConditionNode;

	private INode<IPostResolveAction, NBT> currentSelectedPostResolveActionNode;

	private GuiDropdown<DropdownElementWithData<Supplier<IGuiCondition>>> conditionsDropdown;

	private GuiDropdown<DropdownElementWithData<Supplier<IPostResolveAction>>> postResolveActionDropdown;

	private GuiInput triggerId;

	private GuiButtonVanilla saveButton, closeButton;

	private Blueprint blueprint;

	private GuiDropdownList dropdown;

	private GuiTextureRepeatable triggerBackdrop;

	public GuiEditScheduledRegion(GuiViewer prevFrame, Blueprint blueprint, ISchedule scheduleRegion)
	{
		super(new GuiElement(Dim2D.flush(), false), prevFrame);

		this.blueprint = blueprint;

		this.schedule = scheduleRegion;

		this.allowUserInput = true;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.guiLeft = this.width / 2 - 94;
		this.guiTop = this.height / 2 - 83;

		this.xSize = 179 * 2;
		this.ySize = 169;
	}

	@Override
	public void build(IGuiContext context)
	{
		this.subTreeViewer = new GuiSelectableTree(Dim2D.build().flush());
		this.variablesHeader = new GuiVariablesHeader(Dim2D.build().y(90).flush());

		this.subTreeViewer.setTitle(new TextComponentTranslation("orbis.gui.selected", ""));

		this.triggerBackdrop = new GuiTextureRepeatable(Dim2D.build().width(200).height(90).flush(), CONTAINER, new TextureUV(4, 4, 41, 41), 49, 49,
				TextureRepeatMethods.UNIFORM_EDGES);

		this.triggerBackdrop.dim().add(new RectModifier("xOffsetFromTreeViewer", this.subTreeViewer, RectModifier.ModifierType.WIDTH.getModification(),
				RectModifier.ModifierType.X));

		this.variablesHeader.dim().add(new RectModifier("xOffsetFromTreeViewer", this.subTreeViewer, RectModifier.ModifierType.WIDTH.getModification(),
				RectModifier.ModifierType.X));

		this.variablesHeader.build(this);

		this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));

		this.dropdown = new GuiDropdownList(Dim2D.build().width(60).flush());

		this.dropdown.build(this);

		this.dropdown.state().setZOrder(Integer.MAX_VALUE);

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
							GuiEditScheduledRegion.this.conditionTree.startLinking(ConditionLink.AND);
						}
					});

					elements.add(new DropdownElement(new TextComponentString("Or..."))
					{
						@Override
						public void onClick(final GuiDropdownList list, final EntityPlayer player)
						{
							GuiEditScheduledRegion.this.conditionTree.startLinking(ConditionLink.OR);
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

					return !((n.getParentsIds().isEmpty() || (GuiEditScheduledRegion.this.schedule != null && !roots
							.contains(GuiEditScheduledRegion.this.schedule
									.getConditionNodeTree().getRootNode()))) && n != GuiEditScheduledRegion.this.schedule.getConditionNodeTree()
							.getRootNode());
				},
				(n) ->
				{
					GuiButtonVanilla button = new GuiButtonVanilla(Dim2D.build().width(20).height(20).flush());

					button.getInner().displayString = "C" + String.valueOf(n.getNodeId());

					return button;
				},
				() -> this.schedule.getConditionNodeTree().findNextAvailableId());

		this.conditionTree.setCanDeleteNode((node) -> node.getTree().size() == 1 || node.getTree().getRootNodeId() != node.getNodeId());

		this.postResolveActionTree = new GuiTree<>(Dim2D.build().width(184).height(86).x(8).y(27).flush(), (nodeId) ->
		{
			PostResolveActionMutateBlueprintVariable action = new PostResolveActionMutateBlueprintVariable();

			action.setUsedData(GuiEditScheduledRegion.this.blueprint.getData().getVariableTree());

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
				() -> this.schedule.getPostResolveActionNodeTree().findNextAvailableId());

		this.conditionTree.build(this);
		this.postResolveActionTree.build(this);

		this.conditionTree.state().setZOrder(1);
		this.postResolveActionTree.state().setZOrder(1);

		this.conditionTree.state().addEvent(new MouseInputDisabledWhenNotHovered());
		this.postResolveActionTree.state().addEvent(new MouseInputDisabledWhenNotHovered());

		this.conditionTree.state().setVisible(false);
		this.conditionTree.state().setEnabled(false);

		this.conditionTree.listen(new IGuiTreeListener<IGuiCondition, ConditionLink, GuiButtonVanilla>()
		{

			@Override
			public void onLinkNodes(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> n1,
					INode<IGuiCondition, ConditionLink> n2,
					ConditionLink conditionLink)
			{

				INode<IGuiCondition, ConditionLink> root = GuiEditScheduledRegion.this.schedule.getConditionNodeTree().getRootNode();

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
							GuiEditScheduledRegion.this.schedule.getConditionNodeTree().setRootNode(prominentRoot.getNodeId());
						}
					}
				}
			}

			@Override
			public void onClickNode(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, INode<IGuiCondition, ConditionLink> node)
			{
				GuiEditScheduledRegion.this.varDisplay.updateVariableData();

				GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
				GuiEditScheduledRegion.this.varDisplay.display(node.getData());

				GuiEditScheduledRegion.this.variablesHeader.setTitleAndDropdown(new TextComponentTranslation("orbis.gui.variables",
						I18n.format("orbis.gui.condition") + " " + String.valueOf(node.getNodeId())), GuiEditScheduledRegion.this.conditionsDropdown);

				DropdownElementWithData<Supplier<IGuiCondition>> element = new DropdownElementWithData<>(new TextComponentTranslation(node.getData().getName()),
						null);

				GuiEditScheduledRegion.this.conditionsDropdown.setChosenElement(element);

				GuiEditScheduledRegion.this.currentSelectedNode = node;

				GuiEditScheduledRegion.this.currentSelectedConditionNode = node;
				GuiEditScheduledRegion.this.currentSelectedPostResolveActionNode = null;
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
							dataUser.setUsedData(GuiEditScheduledRegion.this.blueprint.getData().getVariableTree());
						}
					}

					GuiEditScheduledRegion.this.schedule.getConditionNodeTree().put(node.getNodeId(), node);
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
				GuiEditScheduledRegion.this.schedule.getConditionNodeTree().remove(node.getNodeId());

				if (node == GuiEditScheduledRegion.this.currentSelectedNode)
				{
					GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
					GuiEditScheduledRegion.this.varDisplay.reset();

					GuiEditScheduledRegion.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));
				}
			}

			@Override
			public void onMovePane(GuiTree<IGuiCondition, ConditionLink, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiEditScheduledRegion.this.schedule.setConditionGuiPos(pos);
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
				GuiEditScheduledRegion.this.varDisplay.updateVariableData();

				GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
				GuiEditScheduledRegion.this.varDisplay.display(node.getData());

				GuiEditScheduledRegion.this.variablesHeader
						.setTitleAndDropdown(new TextComponentTranslation("orbis.gui.variables",
										I18n.format("orbis.gui.post_resolve_action") + " " + String.valueOf(node.getNodeId())),
								GuiEditScheduledRegion.this.postResolveActionDropdown);

				DropdownElementWithData<Supplier<IPostResolveAction>> element = new DropdownElementWithData<>(
						new TextComponentTranslation(node.getData().getName()),
						null);

				GuiEditScheduledRegion.this.postResolveActionDropdown.setChosenElement(element);

				GuiEditScheduledRegion.this.currentSelectedNode = node;

				GuiEditScheduledRegion.this.currentSelectedConditionNode = null;
				GuiEditScheduledRegion.this.currentSelectedPostResolveActionNode = node;
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
							dataUser.setUsedData(GuiEditScheduledRegion.this.blueprint.getData().getVariableTree());
						}
					}

					GuiEditScheduledRegion.this.schedule.getPostResolveActionNodeTree().put(node.getNodeId(), node);
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
				GuiEditScheduledRegion.this.schedule.getPostResolveActionNodeTree().remove(node.getNodeId());

				if (node == GuiEditScheduledRegion.this.currentSelectedNode)
				{
					GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
					GuiEditScheduledRegion.this.varDisplay.reset();

					GuiEditScheduledRegion.this.variablesHeader.setTitle(new TextComponentTranslation("orbis.gui.variables", ""));
				}
			}

			@Override
			public void onMovePane(GuiTree<IPostResolveAction, NBT, GuiButtonVanilla> tree, Pos2D pos)
			{
				GuiEditScheduledRegion.this.schedule.setPostResolveActionGuiPos(pos);
			}
		});

		this.varDisplay = new GuiVarDisplay(Dim2D.build().width(174).x(5).flush())
		{
			@Override
			public void updateVariableData()
			{
				super.updateVariableData();

				GuiEditScheduledRegion.this.blueprint.markDirty();
			}
		};

		this.varDisplayScrollDecorator = new GuiScrollable(this.varDisplay, Dim2D.build().width(192).flush());

		this.conditionsDropdown = new GuiDropdown<>(Dim2D.build().width(153).flush(), (e) ->
		{
			IGuiCondition condition = e.getData().get();

			condition.setGuiPos(GuiEditScheduledRegion.this.currentSelectedConditionNode.getData().getGuiPos());

			GuiEditScheduledRegion.this.currentSelectedConditionNode.setData(condition);

			GuiEditScheduledRegion.this.varDisplay.updateVariableData();

			GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
			GuiEditScheduledRegion.this.varDisplay.display(condition);
		},
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.percentage"), GuiConditionPercentage::new),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.ratio"), GuiConditionRatio::new),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.check_blueprint_variable"),
						() ->
						{
							GuiConditionCheckBlueprintVariable cond = new GuiConditionCheckBlueprintVariable();

							cond.setUsedData(GuiEditScheduledRegion.this.blueprint.getData().getVariableTree());

							return cond;
						}));

		this.postResolveActionDropdown = new GuiDropdown<>(Dim2D.build().width(153).flush(), (e) ->
		{
			IPostResolveAction action = e.getData().get();

			action.setGuiPos(GuiEditScheduledRegion.this.currentSelectedPostResolveActionNode.getData().getGuiPos());

			GuiEditScheduledRegion.this.currentSelectedPostResolveActionNode.setData(action);

			GuiEditScheduledRegion.this.varDisplay.updateVariableData();

			GuiEditScheduledRegion.this.varDisplayScrollDecorator.resetScroll();
			GuiEditScheduledRegion.this.varDisplay.display(action);
		},
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.mutate_blueprint_variable"), () ->
				{
					PostResolveActionMutateBlueprintVariable action = new PostResolveActionMutateBlueprintVariable();

					action.setUsedData(GuiEditScheduledRegion.this.blueprint.getData().getVariableTree());

					return action;
				}), new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.spawn_entities"), PostResolveActionSpawnEntities::new),
				new DropdownElementWithData<>(new TextComponentTranslation("orbis.gui.apply_loot_table"), PostResolveActionApplyLootTable::new));

		this.varDisplayScrollDecorator.dim()
				.add(new RectModifier("offsetFromVarHeader", this.variablesHeader, (source, modifying) ->
				{
					if (modifying == RectModifier.ModifierType.Y)
					{
						return source.dim().maxY() + 5;
					}

					if (modifying == RectModifier.ModifierType.X)
					{
						return source.dim().x();
					}

					return 0;
				},
						RectModifier.ModifierType.POS));
		this.varDisplayScrollDecorator.dim()
				.add(new RectModifier("heightBelowVarHeader", this.variablesHeader, (source, modifying) -> this.height - source.dim().maxY() - 10,
						RectModifier.ModifierType.HEIGHT));

		this.conditionsDropdown.state().setEnabled(false);
		this.conditionsDropdown.state().setVisible(false);

		this.subTreeViewer.dim().add(new RectModifier("area", this.variablesHeader, (source, modifier) ->
		{
			if (modifier == RectModifier.ModifierType.HEIGHT)
			{
				return this.height;
			}

			if (modifier == RectModifier.ModifierType.WIDTH)
			{
				return this.width - 200;
			}

			return 0;
		}, RectModifier.ModifierType.AREA));

		context.addChildren(this.varDisplayScrollDecorator, this.variablesHeader, this.subTreeViewer);

		RectModifier centerOfTriggerContiner = new RectModifier("pos", this.triggerBackdrop, (source, modifying) ->
		{
			if (modifying == RectModifier.ModifierType.X)
			{
				return source.dim().center().x();
			}

			if (modifying == RectModifier.ModifierType.Y)
			{
				return source.dim().center().y();
			}

			return 0;
		},
				RectModifier.ModifierType.POS);

		final int yOffset = -8;
		int yOffsetInput = 0;
		int xOffsetInput = 0;

		GuiText title = new GuiText(Dim2D.build().width(140).height(20).addY(-25).addX(-32).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush(),
				new Text(new TextComponentString("Trigger ID:"), 1.0F));

		this.triggerId = new GuiInput(Dim2D.build().center(true).width(110).height(20).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.triggerId.getInner().setText(this.schedule.getTriggerId());

		this.saveButton = new GuiButtonVanilla(
				Dim2D.build().center(true).width(50).height(20).addY(30).addX(-30).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.saveButton.getInner().displayString = "Save";

		this.closeButton = new GuiButtonVanilla(
				Dim2D.build().center(true).width(50).height(20).addY(30).addX(30).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.closeButton.getInner().displayString = "Close";

		title.dim().add(centerOfTriggerContiner);
		this.triggerId.dim().add(centerOfTriggerContiner);
		this.saveButton.dim().add(centerOfTriggerContiner);
		this.closeButton.dim().add(centerOfTriggerContiner);

		context.addChildren(this.triggerBackdrop, title, this.triggerId, this.saveButton, this.closeButton);

		context.addChildren(this.dropdown);

		this.subTreeViewer.setTrees(
				new TextComponentTranslation("orbis.gui.selected", ""),
				Pair.of(new TextComponentTranslation("orbis.gui.conditions"), this.conditionTree),
				Pair.of(new TextComponentTranslation("orbis.gui.post_resolve_actions"), this.postResolveActionTree));

		this.conditionTree.reset(this.schedule.getConditionGuiPos());

		for (INode<IGuiCondition, ConditionLink> n : this.schedule.getConditionNodeTree().getNodes())
		{
			this.conditionTree.addNode(n, n.getData().getGuiPos(), true);
		}

		this.postResolveActionTree.reset(this.schedule.getPostResolveActionGuiPos());

		for (INode<IPostResolveAction, NBT> n : this.schedule.getPostResolveActionNodeTree().getNodes())
		{
			this.postResolveActionTree.addNode(n, n.getData().getGuiPos(), true);
		}
	}

	@Override
	protected void keyTypedInner(final char typedChar, final int keyCode) throws IOException
	{
		if (!this.triggerId.getInner().isFocused())
		{
			super.keyTypedInner(typedChar, keyCode);
		}
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.closeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (this.saveButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			OrbisCore.network().sendPacketToServer(
					new PacketSetTriggerId(this.blueprint, this.schedule,
							this.triggerId.getInner().getText()));
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
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public GuiDropdownList getDropdown()
	{
		return this.dropdown;
	}
}
