package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.LayerLink;
import com.gildedgames.orbis_api.core.tree.NodeTreeUtils;
import com.gildedgames.orbis_api.core.variables.displays.GuiTickBox;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayerListener;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiLayerButton extends GuiButtonVanilla implements IScheduleLayerListener
{
	public static final ResourceLocation TICK_BOX_EYE = OrbisCore.getResource("layer_gui/tick_box_eye.png");

	private GuiTickBox hidden;

	private INode<IScheduleLayer, LayerLink> scheduleNode;

	public GuiLayerButton(Rect rect, INode<IScheduleLayer, LayerLink> scheduleNode)
	{
		super(rect);

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

	@Override
	public void onSetVisible(boolean visible)
	{
		this.hidden.setTicked(visible);
	}
}
