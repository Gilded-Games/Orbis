package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.overlays.GodModeOverlay;
import com.gildedgames.orbis.client.overlays.IOverlay;
import com.google.common.collect.Lists;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRenderHandler
{
	private static final List<IOverlay> overlays = Lists.newArrayList();

	static
	{
		ClientRenderHandler.addOverlay(new GodModeOverlay());
	}

	public static void addOverlay(final IOverlay overlay)
	{
		ClientRenderHandler.overlays.add(overlay);
	}

	@SubscribeEvent
	public static void onRenderIngameOverlay(final RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
		{
			for (final IOverlay overlay : ClientRenderHandler.overlays)
			{
				if (overlay.isEnabled())
				{
					overlay.draw();
				}
			}
		}
	}
}
