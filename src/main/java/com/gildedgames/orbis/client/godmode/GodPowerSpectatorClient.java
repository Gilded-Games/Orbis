package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerSpectator;
import com.gildedgames.orbis.lib.world.IWorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerSpectatorClient implements IGodPowerClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/spectator_icon.png");

	private final GodPowerSpectator server;

	private final GuiTexture icon;

	public GodPowerSpectatorClient(final GodPowerSpectator server)
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
		return "Spectator";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean has3DCursor(final PlayerOrbis playerOrbis)
	{
		return false;
	}

	@Override
	public float minFade3DCursor(final PlayerOrbis playerOrbis)
	{
		return 0.0F;
	}

	@Override
	public int getShapeColor(final PlayerOrbis playerOrbis)
	{
		return 0;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerOrbis, final World world)
	{
		return Collections.emptyList();
	}

	@Override
	public Object raytraceObject(PlayerOrbis playerOrbis)
	{
		return null;
	}

	@Override
	public boolean onRightClickShape(PlayerOrbis playerOrbis, Object foundObject, MouseEvent event)
	{
		return false;
	}

	@Override
	public boolean shouldRenderSelection()
	{
		return true;
	}
}
