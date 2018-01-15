package com.gildedgames.orbis.common.player.godmode.selection_input;

import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.client.godmode.selection_inputs.ISelectionInputClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import net.minecraftforge.client.event.MouseEvent;

public interface ISelectionInput extends NBT
{

	boolean shouldClearSelectionOnEscape();

	void clearSelection();

	IWorldObject getActiveSelection();

	void setActiveSelection(IWorldObject activeSelection);

	ISelectionInputClient getClient();

	/**
	 * @param isActive Whether or not this selection input
	 *                 implementation is the actively used
	 *                 implementation.
	 */
	void onUpdate(boolean isActive, IShapeSelector selector);

	void onMouseEvent(MouseEvent event, IShapeSelector selector, PlayerOrbis playerOrbis);

}
