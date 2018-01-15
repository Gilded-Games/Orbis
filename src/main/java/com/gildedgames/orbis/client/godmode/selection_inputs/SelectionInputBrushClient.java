package com.gildedgames.orbis.client.godmode.selection_inputs;

import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class SelectionInputBrushClient implements ISelectionInputClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/selection_mode_icons/brush.png");

	private final GuiTexture icon;

	private RenderShape activeSelectionRender;

	private IWorldObject activeSelection;

	public SelectionInputBrushClient()
	{
		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public String displayName()
	{
		return "Brush";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final ISelectionInput server, final PlayerOrbis playerOrbis, final World world)
	{
		final List<IWorldRenderer> renderers = Lists.newArrayList();

		if (this.activeSelection != server.getActiveSelection())
		{
			this.activeSelection = server.getActiveSelection();
		}

		if (this.activeSelection != null)
		{
			if (this.activeSelectionRender == null)
			{
				this.activeSelectionRender = new RenderShape(this.activeSelection.getShape());

				this.activeSelectionRender.useCustomColors = true;

				this.activeSelectionRender.colorGrid = playerOrbis.powers().getCurrentPower().getClientHandler().getShapeColor(playerOrbis);
				this.activeSelectionRender.colorBorder = this.activeSelectionRender.colorGrid;

				this.activeSelectionRender.boxAlpha = 0.1F;

				this.activeSelectionRender.renderDimensionsAbove = false;
				this.activeSelectionRender.box = false;
			}
			else
			{
				this.activeSelectionRender.colorGrid = playerOrbis.powers().getCurrentPower().getClientHandler().getShapeColor(playerOrbis);
				this.activeSelectionRender.colorBorder = this.activeSelectionRender.colorGrid;

				this.activeSelectionRender.setShape(this.activeSelection.getShape());
			}

			renderers.add(this.activeSelectionRender);
		}
		else
		{
			this.activeSelectionRender = null;
		}

		return renderers;
	}
}
