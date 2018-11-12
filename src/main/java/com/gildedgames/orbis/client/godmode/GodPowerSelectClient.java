package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.client.gui.right_click.*;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerSelectClient implements IGodPowerClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/select_icon.png");

	private static final int SHAPE_COLOR = 0x999999;

	private final GuiTexture icon;

	private final GodPowerSelect server;

	private final ItemStack prevItemstack = null;

	private IWorldRenderer renderer;

	private RenderShape renderShape;

	public GodPowerSelectClient(final GodPowerSelect server)
	{
		this.server = server;

		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public String displayName()
	{
		return "Select";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean has3DCursor(final PlayerOrbis playerOrbis)
	{
		return true;
	}

	@Override
	public float minFade3DCursor(final PlayerOrbis playerOrbis)
	{
		return 0;
	}

	@Override
	public int getShapeColor(final PlayerOrbis playerOrbis)
	{
		return playerOrbis.powers().isScheduling() ? 0xd19044 : SHAPE_COLOR;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerOrbis, final World world)
	{
		return Collections.emptyList();
	}

	@Override
	public Object raytraceObject(PlayerOrbis playerOrbis)
	{
		return playerOrbis.powers().isScheduling() ? playerOrbis.getSelectedSchedule() : playerOrbis.getSelectedRegion();
	}

	@Override
	public boolean onRightClickShape(PlayerOrbis playerOrbis, Object foundObject, MouseEvent event)
	{
		final EntityPlayer entity = playerOrbis.getEntity();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		if (foundObject instanceof IShape)
		{
			IShape selectedShape = (IShape) foundObject;

			final boolean playerInside = selectedShape.contains(x, y, z) || selectedShape.contains(x, MathHelper.floor(entity.posY + entity.height), z);

			if (entity.world.isRemote && !playerInside)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					if (!(selectedShape instanceof Blueprint) && selectedShape instanceof WorldShape)
					{
						Minecraft.getMinecraft().displayGuiScreen(new GuiRightClickSelector(playerOrbis, (WorldShape) selectedShape));
					}
					else if (selectedShape instanceof GhostBlockDataContainer)
					{
						Minecraft.getMinecraft()
								.displayGuiScreen(new GuiRightClickGhostBlockDataContainer(playerOrbis, (GhostBlockDataContainer) selectedShape));
					}
					else
					{
						return true;
					}

					return false;
				}
			}
		}
		else if (foundObject instanceof ScheduleRegion)
		{
			IShape worldObject = playerOrbis.getSelectedRegion();
			ScheduleRegion scheduleRegion = (ScheduleRegion) foundObject;

			if (entity.world.isRemote && worldObject instanceof Blueprint)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					Minecraft.getMinecraft()
							.displayGuiScreen(new GuiRightClickScheduleRegion((Blueprint) worldObject,
									scheduleRegion));

					return false;
				}
			}
		}
		else if (foundObject instanceof ISchedule)
		{
			IShape worldObject = playerOrbis.getSelectedRegion();
			ISchedule schedule = (ISchedule) foundObject;

			if (entity.world.isRemote && worldObject instanceof Blueprint)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					Minecraft.getMinecraft()
							.displayGuiScreen(new GuiRightClickSchedule((Blueprint) worldObject, schedule));

					return false;
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldRenderSelection()
	{
		return true;
	}
}
