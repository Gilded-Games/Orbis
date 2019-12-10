package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.lib.data.region.IDimensions;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.util.RegionHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class RenderUtil
{

	private final static Minecraft mc = Minecraft.getMinecraft();

	public static void transformForWorld(IDimensions dim)
	{
		transformForWorld(dim, 1.0F, 45F);
	}

	public static void transformForWorld(IDimensions dim, float scale, float yRotation)
	{
		int maxval = Math.max(dim.getWidth(), dim.getHeight());
		maxval = Math.max(dim.getLength(), maxval);

		final float scalefactor = Math.min(1, (scale / maxval));

		GlStateManager.translate(0.5F, 0.65F, 0.5F);

		GlStateManager.scale(0.6F, 0.6F, 0.6F);
		GlStateManager.scale(scalefactor, scalefactor, scalefactor);

		GlStateManager.rotate(yRotation, 0.0F, 1.0F, 0.0F);

		GlStateManager.translate(-dim.getWidth() / 2.f, -dim.getHeight() / 2.f, -dim.getLength() / 2.f);
	}

	public static void transformForGui(IDimensions dim)
	{
		transformForGui(dim, 1.0F);
	}

	public static void transformForGui(IDimensions dim, float scale)
	{
		int maxval = Math.max(dim.getWidth(), dim.getHeight());
		maxval = Math.max(dim.getLength(), maxval);

		final float scalefactor = Math.min(1, (scale / maxval));

		GlStateManager.translate(0.5F, 0.5F, 0.5F);

		GlStateManager.scale(0.6F, 0.6F, 0.6F);
		GlStateManager.scale(scalefactor, scalefactor, scalefactor);

		GlStateManager.rotate(45.0F, 0.0F, -1.0F, 0.0F);
		GlStateManager.rotate(30.0F, 1.0F, 0.0F, -1.0F);

		GlStateManager.translate(-dim.getWidth() / 2.f, -dim.getHeight() / 2.f, -dim.getLength() / 2.f);
	}

	public static void rotateRender(IDimensions d, Rotation r)
	{
		float angle = 0.0F;

		switch (r)
		{
			case NONE:
				angle = 0.0F;
				break;
			case CLOCKWISE_90:
				GlStateManager.translate(d.getLength(), 0, 0);

				angle = 270.0F;
				break;
			case CLOCKWISE_180:
				GlStateManager.translate(d.getWidth(), 0, d.getLength());

				angle = 180.0F;
				break;
			case COUNTERCLOCKWISE_90:
				GlStateManager.translate(0, 0, d.getWidth());

				angle = 90.0F;
				break;
		}

		GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
	}

	public static void renderTextAbove(final IRegion region, final String string, final double yOffset, final float partialTicks)
	{
		final double offsetPlayerX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;

		final BlockPos center = RegionHelp.getBottomCenter(region);

		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderUtil.renderLabel(string, -offsetPlayerX + center.getX() + 0.5F, -offsetPlayerY + region.getMax().getY() + yOffset,
				-offsetPlayerZ + center.getZ() + 0.5F);
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
