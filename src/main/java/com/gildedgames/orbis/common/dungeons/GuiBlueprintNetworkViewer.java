package com.gildedgames.orbis.common.dungeons;

import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewerNoContainer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintNetworkData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.processing.BlueprintNetworkGenerator;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class GuiBlueprintNetworkViewer extends GuiViewerNoContainer
{
	private static class NodeToPaint
	{
		private IRegion region;

		private int color, zLevel;

		public NodeToPaint(IRegion region, int color, int zLevel)
		{
			this.region = region;
			this.color = color;
			this.zLevel = zLevel;
		}

		public int getColor()
		{
			return this.color;
		}

		public IRegion getRegion()
		{
			return this.region;
		}

		public Integer getZLevel()
		{
			return this.zLevel;
		}
	}

	private BlueprintNetworkData data;

	private BlueprintNetworkGenerator generator;

	private List<NodeToPaint> nodesToPaint = Lists.newArrayList();

	private BlueprintNetworkGenerator.IDebugNetworkPainter painter = (b, color, zLevel) -> {
		this.nodesToPaint.add(new NodeToPaint(b, color, zLevel));
		this.nodesToPaint.sort(Comparator.comparing(NodeToPaint::getZLevel));
	};

	public GuiBlueprintNetworkViewer(BlueprintNetworkData data)
	{
		super(new GuiElement(Dim2D.flush(), false));

		this.data = data;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
	}

	@Override
	public void build(IGuiContext context)
	{
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawBackground(0xc2beb4);

		for (NodeToPaint node : this.nodesToPaint)
		{
			this.paintRegion(node.getRegion(), node.getColor());
		}
	}

	private void paintRegion(IRegion region, int color)
	{
		BlockPos min = region.getMin();
		int minX = min.getX();
		int minY = min.getZ();

		GlStateManager.pushMatrix();

		int x = minX + (this.width / 2);
		int y = minY + (this.height / 2);

		Gui.drawRect(x, y, x + region.getWidth(), y + region.getLength(), color);

		GlStateManager.popMatrix();
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);

		if (this.generator == null)
		{
			this.nodesToPaint.clear();
			this.generator = new BlueprintNetworkGenerator(this.data, new CreationData(Minecraft.getMinecraft().world), this.painter, (n, p) -> {
			});
		}
		else
		{
			if (this.generator.step())
			{
				this.generator = null;
			}
		}
	}

}
