package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.client.gui.GuiLoadData;
import com.gildedgames.orbis.client.gui.blueprint.GuiEditBlueprintPostGen;
import com.gildedgames.orbis.client.gui.schedules.GuiEditScheduledRegion;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerEditBlueprintPostGen;
import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.common.containers.ContainerScheduleRegion;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.data.schedules.ScheduleRegion;
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
				IShape shape = WorldObjectUtils.getIntersectingShape(world, pos);

				if (shape instanceof Blueprint)
				{
					Blueprint blueprint = (Blueprint) shape;

					ISchedule schedule = blueprint.findIntersectingSchedule(pos);

					if (schedule instanceof ScheduleRegion)
					{
						ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

						return new ContainerScheduleRegion(playerOrbis, scheduleRegion.getSpawnEggsInventory());
					}
				}
				return null;
			case POST_GEN:
				shape = WorldObjectUtils.getIntersectingShape(world, pos);

				if (shape instanceof Blueprint)
				{
					Blueprint blueprint = (Blueprint) shape;

					return new ContainerEditBlueprintPostGen(playerOrbis, blueprint);
				}

				return null;
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
				IShape shape = WorldObjectUtils.getIntersectingShape(world, pos);

				if (shape instanceof Blueprint)
				{
					Blueprint blueprint = (Blueprint) shape;

					ISchedule schedule = blueprint.findIntersectingSchedule(pos);

					if (schedule instanceof ScheduleRegion)
					{
						ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

						return new GuiEditScheduledRegion(null, playerOrbis, scheduleRegion);
					}
				}
				return null;
			case POST_GEN:
				shape = WorldObjectUtils.getIntersectingShape(world, pos);

				if (shape instanceof Blueprint)
				{
					Blueprint blueprint = (Blueprint) shape;
					GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

					return new GuiEditBlueprintPostGen(screen instanceof GuiFrame ? (GuiFrame) screen : null, blueprint);
				}

				return null;
			default:
				return null;
		}
	}
}
