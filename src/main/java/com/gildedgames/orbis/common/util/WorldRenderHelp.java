package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import net.minecraft.client.Minecraft;

public class WorldRenderHelp
{

	private static Minecraft mc = Minecraft.getMinecraft();

	public static void renderSubRenderers(IWorldRenderer top)
	{
		for (IWorldRenderer renderer : top.getSubRenderers(mc.world))
		{
			renderer.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

			renderSubRenderers(renderer);
		}
	}

}
