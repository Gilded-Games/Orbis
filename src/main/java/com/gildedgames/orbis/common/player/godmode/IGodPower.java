package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import net.minecraft.entity.player.EntityPlayer;

public interface IGodPower<C extends IGodPowerClient> extends NBT
{

	void onUpdate(EntityPlayer player, PlayerOrbis playerOrbis, boolean isPowerActive);

	boolean hasCustomGui(PlayerOrbis playerOrbis);

	void onOpenGui(EntityPlayer player);

	boolean canInteractWithItems(PlayerOrbis playerOrbis);

	/** If the returned IShapeSelector is null, this GodPower cannot
	 * select shapes in the world.
	 * @return
	 */
	IShapeSelector getShapeSelector();

	C getClientHandler();

}
