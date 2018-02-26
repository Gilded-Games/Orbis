package com.gildedgames.orbis.client.overlays;

import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.client.godmode.selection_inputs.ISelectionInputClient;
import com.gildedgames.orbis.client.godmode.selection_types.ISelectionTypeClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GodModeOverlay implements IOverlay
{

	private final static ResourceLocation BACKDROP_TEXTURE = OrbisCore.getResource("godmode/overlay/hotbar_power.png");

	private final static ResourceLocation GENERATE_ICON = OrbisCore.getResource("godmode/placement_icons/generate_icon.png");

	private final static ResourceLocation SCHEDULE_ICON = OrbisCore.getResource("godmode/placement_icons/schedule_icon.png");

	private static final Minecraft mc = Minecraft.getMinecraft();

	public GodModeOverlay()
	{

	}

	@Override
	public boolean isEnabled()
	{
		return mc.world != null && PlayerOrbis.get(mc.player).inDeveloperMode() && mc.player.isCreative();
	}

	@Override
	public void draw()
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();

		mc.getTextureManager().bindTexture(BACKDROP_TEXTURE);

		final int centerX = (int) InputHelper.getScreenWidth() / 2;
		final int centerZ = (int) (InputHelper.getScreenHeight() - 42);

		Gui.drawModalRectWithCustomSizedTexture(centerX - (45), centerZ, 0, 0, 90, 20, 90, 20);
		final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);

		final IGodPowerClient powerClient = playerOrbis.powers().getCurrentPower().getClientHandler();

		mc.getTextureManager().bindTexture(powerClient.getIcon().getResourceLocation());

		int width = (int) powerClient.getIcon().dim().originalState().width();
		int height = (int) powerClient.getIcon().dim().originalState().height();

		Gui.drawModalRectWithCustomSizedTexture(centerX - (width / 2), centerZ + 5, 0, 0, width, height, width, height);

		final ISelectionTypeClient selectionClient = playerOrbis.selectionTypes().getCurrentSelectionType().getClient();

		mc.getTextureManager().bindTexture(selectionClient.getIcon().getResourceLocation());

		width = (int) selectionClient.getIcon().dim().originalState().width();
		height = (int) selectionClient.getIcon().dim().originalState().height();

		GlStateManager.translate(centerX + 16, centerZ + 9, 0);

		GlStateManager.scale(0.75F, 0.75F, 0.0F);

		Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();

		final ISelectionInputClient selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput().getClient();

		mc.getTextureManager().bindTexture(selectionInput.getIcon().getResourceLocation());

		width = (int) selectionInput.getIcon().dim().originalState().width();
		height = (int) selectionInput.getIcon().dim().originalState().height();

		GlStateManager.translate(centerX + 34.0F, centerZ + 13.5F, 0);

		GlStateManager.scale(0.5F, 0.5F, 0.0F);

		Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();

		mc.getTextureManager().bindTexture(playerOrbis.powers().isScheduling() ? SCHEDULE_ICON : GENERATE_ICON);

		width = 16;
		height = 16;

		GlStateManager.translate(centerX - 27, centerZ + 8, 0);

		GlStateManager.scale(0.75F, 0.75F, 0.0F);

		Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();
	}

}
