package com.gildedgames.orbis.client.godmode;

import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.gui.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.framework.GuiRightClickFramework;
import com.gildedgames.orbis.client.gui.framework.GuiRightClickFrameworkNode;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.Framework;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import java.util.Collections;
import java.util.List;

public class GodPowerFrameworkClient implements IGodPowerClient
{
	public static final int SHAPE_COLOR = 0x9f73d4;

	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/power_icons/framework_icon.png");

	private final GuiTexture icon;

	public GodPowerFrameworkClient()
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
		return "Framework";
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
		final EntityPlayer entity = playerOrbis.getEntity();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		Object foundObject = playerOrbis.getSelectedRegion(Framework.class);

		if (foundObject != null)
		{
			Framework framework = (Framework) foundObject;

			final boolean playerInside = framework.contains(x, y, z) || framework.contains(x, MathHelper.floor(entity.posY + entity.height), z);

			if (playerInside)
			{
				return playerOrbis.getSelectedNode();
			}
		}

		return foundObject;
	}

	@Override
	public boolean onRightClickShape(PlayerOrbis playerOrbis, Object foundObject, MouseEvent event)
	{
		final EntityPlayer entity = playerOrbis.getEntity();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		if (foundObject instanceof Framework)
		{
			Framework framework = (Framework) foundObject;

			final boolean playerInside = framework.contains(x, y, z) || framework.contains(x, MathHelper.floor(entity.posY + entity.height), z);

			if (entity.world.isRemote && !playerInside)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiRightClickFramework(framework));
				}
			}

			return false;
		}
		else if (foundObject instanceof IFrameworkNode)
		{
			IFrameworkNode node = (IFrameworkNode) foundObject;

			if (entity.world.isRemote)
			{
				if (System.currentTimeMillis() - GuiRightClickElements.lastCloseTime > 200)
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiRightClickFrameworkNode((Framework) node.getWorldObjectParent(), node));
				}
			}

			return false;
		}

		return true;
	}

	@Override
	public boolean shouldRenderSelection()
	{
		return true;
	}
}
