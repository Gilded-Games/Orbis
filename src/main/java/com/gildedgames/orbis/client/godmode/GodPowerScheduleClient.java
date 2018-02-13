package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerSchedule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerScheduleClient implements IGodPowerClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/schedule_icon.png");

	private static final int SHAPE_COLOR = 0xd19044;

	private final GodPowerSchedule server;

	private final GuiTexture icon;

	public GodPowerScheduleClient(final GodPowerSchedule server)
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
		return "Schedule";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}

	@Override
	public boolean has3DCursor(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return held.getItem() instanceof ItemBlock;
	}

	@Override
	public float minFade3DCursor(final PlayerOrbis playerORbis)
	{
		return 0.0F;
	}

	@Override
	public int getShapeColor(final PlayerOrbis playerORbis)
	{
		return SHAPE_COLOR;
	}

	@Override
	public List<IWorldRenderer> getActiveRenderers(final PlayerOrbis playerORbis, final World world)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean onRightClickShape(final PlayerOrbis playerOrbis, final IShape selectedShape, final MouseEvent event)
	{
		return true;
	}

	@Override
	public boolean shouldRenderSelection()
	{
		return true;
	}
}
