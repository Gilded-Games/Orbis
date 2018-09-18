package com.gildedgames.orbis.client.renderers;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class CachedRender
{

	private RenderInner render;

	private VertexBuffer vbo;

	private VertexFormat format;

	public CachedRender(VertexFormat format, RenderInner render)
	{
		this.format = format;
		this.render = render;
	}

	private void cache(World world, float partialTicks)
	{
		this.vbo = new VertexBuffer(this.format);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		buffer.begin(GL11.GL_QUADS, this.format);

		this.render.render(this.format, buffer, world, partialTicks);

		buffer.finishDrawing();
		buffer.reset();

		this.vbo.bufferData(buffer.getByteBuffer());

		buffer.setTranslation(0, 0, 0);
	}

	public void render(World world, float partialTicks)
	{
		if (this.vbo == null)
		{
			this.cache(world, partialTicks);
		}

		GlStateManager.glEnableClientState(32884);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.glEnableClientState(32888);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.glEnableClientState(32888);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.glEnableClientState(32886);

		this.vbo.bindBuffer();

		this.setupArrayPointers();
		this.vbo.drawArrays(7);

		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
		GlStateManager.resetColor();

		for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
		{
			VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
			int k1 = vertexformatelement.getIndex();

			switch (vertexformatelement$enumusage)
			{
				case POSITION:
					GlStateManager.glDisableClientState(32884);
					break;
				case UV:
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
					GlStateManager.glDisableClientState(32888);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
					break;
				case COLOR:
					GlStateManager.glDisableClientState(32886);
					GlStateManager.resetColor();
			}
		}
	}

	private void setupArrayPointers()
	{
		GlStateManager.glVertexPointer(3, 5126, 28, 0);
		GlStateManager.glColorPointer(4, 5121, 28, 12);
		GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public void reset()
	{
		if (this.vbo != null)
		{
			this.vbo.deleteGlBuffers();
		}

		this.vbo = null;
	}

	public interface RenderInner
	{
		void render(VertexFormat format, BufferBuilder buffer, World world, float partialTicks);
	}
}
