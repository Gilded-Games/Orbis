package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.RegionHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.BlockPos;

public class RenderUtil
{

	private final static Minecraft mc = Minecraft.getMinecraft();

	public static void renderTextAbove(final IRegion region, final String string, final double yOffset, final float partialTicks)
	{
		final double offsetPlayerX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;

		final BlockPos center = RegionHelp.getBottomCenter(region);
		RenderUtil.renderLabel(string, -offsetPlayerX + center.getX(), -offsetPlayerY + region.getMax().getY() + yOffset, -offsetPlayerZ + center.getZ());
	}

	public static void renderDimensionsAbove(final IRegion region, final float partialTicks)
	{
		final String coordinates = String.format("%sx%sx%s", region.getWidth(), region.getHeight(), region.getLength());

		renderTextAbove(region, coordinates, 1.5D, partialTicks);
	}

	public static void renderLabel(final String name, final double x, final double y, final double z)
	{
		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

		// TODO: Implement distance check
		if (renderManager.options != null)
		{
			final float f = renderManager.playerViewY;
			final float f1 = renderManager.playerViewX;
			final boolean flag1 = renderManager.options.thirdPersonView == 2;

			EntityRenderer.drawNameplate(renderManager.getFontRenderer(), name, (float) x, (float) y, (float) z, 0, f, f1, flag1, false);
		}
	}

}
