package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickEntrance;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerEntranceClient implements IGodPowerClient
{
	public static final int SHAPE_COLOR = 0xd38dc7;

	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/entrance_icon.png");

	private final GuiTexture icon;

	public GodPowerEntranceClient()
	{
		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public String displayName()
	{
		return "Entrance";
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
		return SHAPE_COLOR;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerOrbis, final World world)
	{
		return Collections.emptyList();
	}

	@Override
	public Object raytraceObject(PlayerOrbis playerOrbis)
	{
		return playerOrbis.getSelectedEntrance();
	}

	@Override
	public boolean onRightClickShape(PlayerOrbis playerOrbis, Object foundObject, MouseEvent event)
	{
		final EntityPlayer entity = playerOrbis.getEntity();

		if (foundObject instanceof Entrance)
		{
			Entrance entrance = (Entrance) foundObject;

			if (entity.world.isRemote)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					Minecraft.getMinecraft()
							.displayGuiScreen(new GuiRightClickEntrance((Blueprint) entrance.getWorldObjectParent(), entrance));

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
