package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.client.gui.GuiLoadData;
import com.gildedgames.orbis.client.gui.blueprint.GuiEditBlueprintPostGen;
import com.gildedgames.orbis.client.gui.schedules.GuiEditScheduledRegion;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerEditBlueprintPostGen;
import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.util.mc.ContainerGeneric;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OrbisGuiHandler implements IGuiHandler
{

	public static final int LOAD_DATA = 1;

	public static final int EDIT_SCHEDULE_REGION = 2;

	public static final int POST_GEN = 3;

	@Override
	public Container getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
	{
		final BlockPos pos = new BlockPos(x, y, z);

		PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		switch (id)
		{
			case LOAD_DATA:
				return new ContainerLoadData(playerOrbis, playerOrbis.powers().getBlueprintPower().getForgeInventory());
			case EDIT_SCHEDULE_REGION:
				return new ContainerGeneric();
			case POST_GEN:
				return WorldObjectUtils.getIntersectingShape(world, pos)
						.filter(Blueprint.class::isInstance)
						.map(Blueprint.class::cast)
						.map(blueprint -> new ContainerEditBlueprintPostGen(playerOrbis,blueprint)).orElse(null);
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
			case LOAD_DATA:
				return new GuiLoadData(null, playerOrbis);
			case EDIT_SCHEDULE_REGION:
				return WorldObjectUtils.getIntersectingShape(world, pos)
						.filter(Blueprint.class::isInstance)
						.map(Blueprint.class::cast)
						.map(blueprint -> blueprint.findIntersectingSchedule(pos).map(schedule -> new GuiEditScheduledRegion(null,blueprint,schedule)).orElse(null)).orElse(null);
			case POST_GEN:
				GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;
				return WorldObjectUtils.getIntersectingShape(world, pos)
						.filter(Blueprint.class::isInstance)
						.map(Blueprint.class::cast)
						.map(blueprint -> new GuiEditBlueprintPostGen(screen instanceof GuiViewer? (GuiViewer)screen:null, blueprint)).orElse(null);
			default:
				return null;
		}
	}
}
