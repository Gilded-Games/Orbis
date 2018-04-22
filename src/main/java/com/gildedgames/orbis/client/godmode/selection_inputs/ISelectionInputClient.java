package com.gildedgames.orbis.client.godmode.selection_inputs;

import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import net.minecraft.world.World;

import java.util.List;

public interface ISelectionInputClient
{

	String displayName();

	GuiTexture getIcon();

	List<IWorldRenderer> getActiveRenderers(ISelectionInput server, final PlayerOrbis playerOrbis, final World world);

}
