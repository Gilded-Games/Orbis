package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.client.rect.Rect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiTexture extends GuiFrame
{
	private ResourceLocation texture;

	public GuiTexture(final Rect rect, final ResourceLocation texture)
	{
		super(rect);

		this.texture = texture;
		this.setShouldScaleRender(false);
	}

	public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth,
			float textureHeight)
	{
		float f = 1.0F / textureWidth;
		float f1 = 1.0F / textureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double) x, (double) (y + height), 0.0D).tex((double) (u * f), (double) ((v + height) * f1)).endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + height), 0.0D).tex((double) ((u + width) * f), (double) ((v + height) * f1))
				.endVertex();
		bufferbuilder.pos((double) (x + width), (double) y, 0.0D).tex((double) ((u + width) * f), (double) (v * f1)).endVertex();
		bufferbuilder.pos((double) x, (double) y, 0.0D).tex((double) (u * f), (double) (v * f1)).endVertex();
		tessellator.draw();
	}

	public ResourceLocation getResourceLocation()
	{
		return this.texture;
	}

	public void setResourceLocation(final ResourceLocation texture)
	{
		this.texture = texture;
	}

	@Override
	public void init()
	{

	}

	@Override
	public void draw()
	{
		GlStateManager.pushMatrix();

		GuiFrameUtils.applyAlpha(this);

		this.mc.getTextureManager().bindTexture(this.texture);

		drawModalRectWithCustomSizedTexture(this.dim().x(), this.dim().y(), 0, 0, this.dim().width(),
				this.dim().height(),
				this.dim().width(), this.dim().height());

		GlStateManager.popMatrix();
	}

	@Override
	public GuiTexture clone()
	{
		return new GuiTexture(this.dim(), this.texture);
	}
}
