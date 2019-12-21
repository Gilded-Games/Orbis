package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.world.IWorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.List;

public interface IGodPowerClient
{

	void onOpenGui(EntityPlayer player);

	String displayName();

	GuiTexture getIcon();

	boolean has3DCursor(PlayerOrbis playerOrbis);

	float minFade3DCursor(PlayerOrbis playerOrbis);

	int getShapeColor(PlayerOrbis playerOrbis);

	boolean shouldRenderSelection();

	List<IWorldRenderer> getActiveRenderers(PlayerOrbis playerOrbis, World world);

	Object raytraceObject(PlayerOrbis playerOrbis);

	/**
	 * @return True if it should use the regular active selection behaviour. False to cancel.
	 */
	boolean onRightClickShape(PlayerOrbis playerOrbis, Object foundObject, MouseEvent event);

}
