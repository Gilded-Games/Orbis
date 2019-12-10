package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.client.rect.Rect;
import com.gildedgames.orbis.lib.core.tree.INode;
import com.gildedgames.orbis.lib.core.tree.LayerLink;
import com.gildedgames.orbis.lib.core.tree.NodeTreeUtils;
import com.gildedgames.orbis.lib.core.variables.displays.GuiTickBox;
import com.gildedgames.orbis.lib.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.lib.data.schedules.IScheduleLayerListener;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiLayerButton extends GuiButtonVanilla implements IScheduleLayerListener
{
	public static final ResourceLocation TICK_BOX_EYE = OrbisCore.getResource("layer_gui/tick_box_eye.png");

	public static final ResourceLocation TICK_BOX_EYE_SELECTED = OrbisCore.getResource("layer_gui/tick_box_eye_selected.png");

	private GuiTickBox hidden;

	private INode<IScheduleLayer, LayerLink> scheduleNode;

	private GuiLayerEditor editor;

	private boolean selected,previousState;

	public GuiLayerButton(GuiLayerEditor editor, Rect rect, INode<IScheduleLayer, LayerLink> scheduleNode)
	{
		super(rect);

		this.editor = editor;

		this.scheduleNode = scheduleNode;

		this.scheduleNode.getData().listen(this);
	}

	@Override
	public void build()
	{
		this.hidden = new GuiTickBox(Pos2D.flush(3, 2), this.scheduleNode.getData().isVisible(), TICK_BOX_EYE);

		this.hidden.build(this.viewer());

		this.hidden.state().setZOrder(1);

		this.hidden.listenOnPress((ticked) ->
		{
			if (selected && !ticked)
			{
				this.hidden.setTicked(true);
				return;
			}
			this.scheduleNode.getData().setVisible(ticked);

			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				List<INode<IScheduleLayer, LayerLink>> children = Lists.newArrayList();

				NodeTreeUtils.fetchAllChildren(this.scheduleNode, children);

				for (INode<IScheduleLayer, LayerLink> child : children)
				{
					child.getData().setVisible(ticked);
				}
			}
		});
		this.context().addChildren(this.hidden);

		this.state().setCanBeTopHoverElement(true);
	}

	public void setSelected(boolean flag)
	{
		if (!flag)
		{
			selected = false;
			this.hidden.setTicked(previousState);
			this.hidden.setTickedTexture(TICK_BOX_EYE);
		}
		else
		{
			selected = true;
			previousState = this.hidden.isTicked();
			this.hidden.setTicked(true);
			this.hidden.setTickedTexture(TICK_BOX_EYE_SELECTED);
		}
	}

	@Override
	public void onSetVisible(boolean visible)
	{
		this.hidden.setTicked(visible);
	}
}
