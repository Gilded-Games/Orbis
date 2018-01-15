package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.client.gui.GuiLoadBlueprint;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerBlueprintInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OrbisGuiHandler implements IGuiHandler
{

	public static final int ORBIS_BLUEPRINT_LOAD = 1;

	@Override
	public Container getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
	{
		final BlockPos pos = new BlockPos(x, y, z);

		PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		switch (id)
		{
			case ORBIS_BLUEPRINT_LOAD:
				return new ContainerBlueprintInventory(playerOrbis, playerOrbis.powers().getBlueprintPower().getForgeInventory());
			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiContainer getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
	{
		final BlockPos pos = new BlockPos(x, y, z);

		PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		switch (id)
		{
			case ORBIS_BLUEPRINT_LOAD:
				return new GuiLoadBlueprint(playerOrbis);
			default:
				return null;
		}
	}
}
