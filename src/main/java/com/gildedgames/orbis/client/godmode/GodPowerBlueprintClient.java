package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.gui.GuiRightClickBlueprint;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.renderers.RenderBlueprintBlocks;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintPalette;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerBlueprintClient implements IGodPowerClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/blueprint_icon.png");

	private static final int SHAPE_COLOR = 0x99B6FF;

	private final GodPowerBlueprint server;

	private final GuiTexture icon;

	private final List<IWorldRenderer> paletteRenderers = Lists.newArrayList();

	private final List<Blueprint> paletteBlueprints = Lists.newArrayList();

	private Blueprint blueprint;

	private IWorldRenderer renderer;

	private RenderShape renderShape;

	private BlueprintData prevBlueprintData;

	private BlueprintPalette prevPalette;

	private Rotation prevRotation;

	public GodPowerBlueprintClient(final GodPowerBlueprint server)
	{
		this.server = server;

		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public String displayName()
	{
		return "Blueprint";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean has3DCursor(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return held.isEmpty();
	}

	@Override
	public float minFade3DCursor(final PlayerOrbis playerOrbis)
	{
		return 0.0F;
	}

	@Override
	public int getShapeColor(final PlayerOrbis playerOrbis)
	{
		return SHAPE_COLOR;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerOrbis, final World world)
	{
		final List<IWorldRenderer> renderers = Lists.newArrayList();

		if (playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getPathwayPower())
		{
			return Collections.emptyList();
		}

		if (this.prevBlueprintData != this.server.getPlacingBlueprint() || this.prevRotation != this.server.getPlacingRotation())
		{
			this.renderer = null;

			this.prevBlueprintData = this.server.getPlacingBlueprint();
			this.prevRotation = this.server.getPlacingRotation();
		}

		if (this.prevPalette != this.server.getPlacingPalette() || this.prevRotation != this.server.getPlacingRotation())
		{
			this.paletteRenderers.clear();
			this.paletteBlueprints.clear();

			this.prevPalette = this.server.getPlacingPalette();
			this.prevRotation = this.server.getPlacingRotation();
		}

		if (this.server.getPlacingPalette() != null)
		{
			final BlockPos pos = RaytraceHelp.doOrbisRaytrace(playerOrbis, playerOrbis.raytraceWithRegionSnapping());

			final Blueprint blueprint;

			if (this.paletteRenderers.isEmpty() && this.paletteBlueprints.isEmpty())
			{
				for (final BlueprintData data : this.server.getPlacingPalette().getData())
				{
					final IRegion region = RotationHelp.regionFromCenter(pos, data, this.server.getPlacingRotation());

					final Blueprint b = new Blueprint(world, region.getMin(), this.server.getPlacingRotation(), data);

					final IWorldRenderer r = new RenderBlueprintBlocks(b, world);

					this.paletteBlueprints.add(b);
					this.paletteRenderers.add(r);
				}

				blueprint = this.paletteBlueprints.get((int) ((System.currentTimeMillis() / 1000) % this.paletteBlueprints.size()));

				this.renderShape = new RenderShape(blueprint);

				this.renderShape.useCustomColors = true;

				this.renderShape.colorBorder = SHAPE_COLOR;
				this.renderShape.colorGrid = SHAPE_COLOR;

				this.renderShape.boxAlpha = 0.1F;
			}
			else
			{
				blueprint = this.paletteBlueprints.get((int) ((System.currentTimeMillis() / 1000) % this.paletteBlueprints.size()));

				final IRegion renderRegion = RotationHelp
						.regionFromCenter(pos, blueprint.getData(), this.server.getPlacingRotation());

				blueprint.setPos(renderRegion.getMin());

				this.renderShape.setShape(blueprint);
			}

			this.paletteRenderers.forEach(r -> r.setDisabled(true));

			final IWorldRenderer rendered = this.paletteRenderers.get((int) ((System.currentTimeMillis() / 1000) % this.paletteBlueprints.size()));

			rendered.setDisabled(false);

			renderers.addAll(this.paletteRenderers);
			renderers.add(this.renderShape);
		}

		if (this.server.getPlacingBlueprint() != null)
		{
			final BlockPos pos = RaytraceHelp.doOrbisRaytrace(playerOrbis, playerOrbis.raytraceWithRegionSnapping());

			final IRegion renderRegion = RotationHelp.regionFromCenter(pos, this.server.getPlacingBlueprint(), this.server.getPlacingRotation());

			if (this.renderer == null)
			{
				this.blueprint = new Blueprint(world, renderRegion.getMin(), this.server.getPlacingRotation(), this.server.getPlacingBlueprint());

				this.renderer = new RenderBlueprintBlocks(this.blueprint, world);

				this.renderShape = new RenderShape(this.blueprint);

				this.renderShape.useCustomColors = true;

				this.renderShape.colorBorder = SHAPE_COLOR;
				this.renderShape.colorGrid = SHAPE_COLOR;

				this.renderShape.boxAlpha = 0.1F;
			}
			else
			{
				this.blueprint.setPos(renderRegion.getMin());
			}

			renderers.add(this.renderer);
			renderers.add(this.renderShape);
		}

		return renderers;
	}

	@Override
	public boolean onRightClickShape(final PlayerOrbis playerOrbis, final IShape selectedShape, final MouseEvent event)
	{
		final EntityPlayer entity = playerOrbis.getEntity();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		if (selectedShape instanceof Blueprint)
		{
			final boolean playerInside = selectedShape.contains(x, y, z) || selectedShape.contains(x, MathHelper.floor(entity.posY + entity.height), z);

			if (entity.world.isRemote && !playerInside)
			{
				if (System.currentTimeMillis() - GuiRightClickBlueprint.lastCloseTime > 200)
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiRightClickBlueprint((Blueprint) selectedShape));
				}
			}

			return false;
		}

		return true;
	}
}
