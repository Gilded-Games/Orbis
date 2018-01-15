package com.gildedgames.orbis.common.items.util;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraftforge.client.event.MouseEvent;

public interface ItemStackInput
{

	void onUpdateInHand(PlayerOrbis playerOrbis);

	void onMouseEvent(MouseEvent event, PlayerOrbis playerOrbis);

}
