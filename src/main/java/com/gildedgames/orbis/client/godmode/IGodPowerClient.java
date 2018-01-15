package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
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

	List<IWorldRenderer> getActiveRenderers(PlayerOrbis playerOrbis, World world);

	/**
	 * @return True if it should use the regular active selection behaviour. False to cancel.
	 */
	boolean onRightClickShape(PlayerOrbis playerOrbis, IShape selectedShape, MouseEvent event);

}
