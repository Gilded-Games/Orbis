package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.renderers.RenderPathway;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.player.godmode.GodPowerPathway;
import com.gildedgames.orbis.common.player.godmode.selection_input.SelectionInputDragged;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerPathwayClient implements IGodPowerClient
{
	public static final int SHAPE_COLOR = 0xe5dab3;

	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/pathway_icon.png");

	private final GuiTexture icon;

	private BlockPos prevEndPos;

	private RenderPathway renderPathway;

	public GodPowerPathwayClient()
	{
		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public String displayName()
	{
		return "Pathway";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean has3DCursor(final PlayerOrbis playerOrbis)
	{
		return true;
	}

	@Override
	public float minFade3DCursor(final PlayerOrbis playerOrbis)
	{
		return 0;
	}

	@Override
	public int getShapeColor(final PlayerOrbis playerOrbis)
	{
		return SHAPE_COLOR;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerOrbis, final World world)
	{
		if (playerOrbis.powers().getCurrentPower() != playerOrbis.powers().getPathwayPower())
		{
			return Collections.emptyList();
		}

		GodPowerBlueprint bPower = playerOrbis.powers().getBlueprintPower();

		List<IWorldRenderer> renderers = Lists.newArrayList();

		if (playerOrbis.selectionInputs().getCurrentSelectionInput() instanceof SelectionInputDragged)
		{
			SelectionInputDragged input = (SelectionInputDragged) playerOrbis.selectionInputs().getCurrentSelectionInput();

			final BlockPos endPos = RaytraceHelp.doOrbisRaytrace(playerOrbis);

			if (!endPos.equals(this.prevEndPos) && input.getSelectPos() != null)
			{
				this.prevEndPos = endPos;

				if (bPower.getPlacingBlueprint() != null || bPower.getPlacingPalette() != null)
				{
					GodPowerPathway p = playerOrbis.powers().getPathwayPower();

					p.processPathway(playerOrbis, input.getSelectPos(), endPos);

					this.renderPathway = new RenderPathway(p.getActivePieces(playerOrbis), p.getStepAStar().currentState(),
							p.getPathwayProblem().getBoundingBox());
				}
			}

			if (this.renderPathway != null && input.getSelectPos() != null)
			{
				renderers.add(this.renderPathway);
			}
		}

		return renderers;
	}

	@Override
	public boolean onRightClickShape(final PlayerOrbis playerOrbis, final IShape selectedShape, final MouseEvent event)
	{
		return true;
	}
}
