package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.gui.util.GuiText;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetScheduling;
import com.gildedgames.orbis.common.network.packets.PacketTeleportOrbis;
import com.gildedgames.orbis.common.util.InputHelper;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiChoiceMenuHolder extends GuiFrame
{
	private final static ResourceLocation CHOICE_BAR = OrbisCore.getResource("godmode/overlay/choice_tab_bar.png");

	private final static ResourceLocation CHOICE_TAB = OrbisCore.getResource("godmode/overlay/choice_tab.png");

	private final static ResourceLocation CHOICE_TAB_UNPRESSED = OrbisCore.getResource("godmode/overlay/choice_tab_unpressed.png");

	private final static ResourceLocation CHOICE_TAB_LEFT = OrbisCore.getResource("godmode/overlay/choice_tab_left.png");

	private final static ResourceLocation CHOICE_TAB_LEFT_UNPRESSED = OrbisCore.getResource("godmode/overlay/choice_tab_left_unpressed.png");

	private final static ResourceLocation CHOICE_TAB_RIGHT = OrbisCore.getResource("godmode/overlay/choice_tab_right.png");

	private final static ResourceLocation CHOICE_TAB_RIGHT_UNPRESSED = OrbisCore.getResource("godmode/overlay/choice_tab_right_unpressed.png");

	private final static ResourceLocation PLACEMENT_MODE_BAR = OrbisCore.getResource("godmode/overlay/placement_mode_bar.png");

	private final static ResourceLocation LEFT_PLACEMENT_MODE = OrbisCore.getResource("godmode/overlay/left_placement_mode.png");

	private final static ResourceLocation RIGHT_PLACEMENT_MODE = OrbisCore.getResource("godmode/overlay/right_placement_mode.png");

	private final static ResourceLocation LEFT_PLACEMENT_MODE_UNPRESSED = OrbisCore.getResource("godmode/overlay/left_placement_mode_unpressed.png");

	private final static ResourceLocation RIGHT_PLACEMENT_MODE_UNPRESSED = OrbisCore.getResource("godmode/overlay/right_placement_mode_unpressed.png");

	private final static ResourceLocation GENERATE_ICON = OrbisCore.getResource("godmode/placement_icons/generate_icon.png");

	private final static ResourceLocation SCHEDULE_ICON = OrbisCore.getResource("godmode/placement_icons/schedule_icon.png");

	private final static ResourceLocation PORTAL_TEXTURE = OrbisCore.getResource("godmode/overlay/portal.png");

	private final static ResourceLocation PORTAL_BACK_TEXTURE = OrbisCore.getResource("godmode/overlay/portal_back.png");

	private static int choicePageIndex;

	private final GuiChoiceMenu[] menus;

	private final GuiTexture[] tabs;

	private GuiTexture left, right, portal;

	private GuiText portalTitle;

	public GuiChoiceMenuHolder(final GuiChoiceMenu... menus)
	{
		this.menus = menus;
		this.tabs = new GuiTexture[this.menus.length];
	}

	public GuiTexture getCurrentTab()
	{
		return this.tabs[choicePageIndex];
	}

	public GuiChoiceMenu getCurrentMenu()
	{
		return this.menus[choicePageIndex];
	}

	private void setCurrentPage(final int index)
	{
		if (index >= this.menus.length)
		{
			return;
		}

		choicePageIndex = index;

		if (choicePageIndex >= this.menus.length)
		{
			choicePageIndex = 0;
		}

		if (choicePageIndex < 0)
		{
			choicePageIndex = this.menus.length - 1;
		}

		for (final GuiChoiceMenu menu : this.menus)
		{
			menu.setVisible(false);
			menu.setEnabled(false);
		}

		int i = 0;

		for (final GuiTexture tab : this.tabs)
		{
			tab.setResourceLocation(i == 0 ? CHOICE_TAB_LEFT_UNPRESSED : i == 2 ? CHOICE_TAB_RIGHT_UNPRESSED : CHOICE_TAB_UNPRESSED);

			i++;
		}

		this.getCurrentMenu().setEnabled(true);
		this.getCurrentMenu().setVisible(true);

		this.getCurrentTab().setResourceLocation(choicePageIndex == 0 ? CHOICE_TAB_LEFT : choicePageIndex == 2 ? CHOICE_TAB_RIGHT : CHOICE_TAB);
	}

	@Override
	public void onMouseWheel(final int state)
	{
		if (state > 0)
		{
			this.setCurrentPage(choicePageIndex + 1);
		}
		else if (state < 0)
		{
			this.setCurrentPage(choicePageIndex - 1);
		}
	}

	@Override
	public void draw()
	{
		if (InputHelper.isHovered(this.portal))
		{
			this.portal.dim().mod().scale(1.1F).flush();

			this.portalTitle.setVisible(true);
		}
		else
		{
			this.portal.dim().mod().scale(1.0F).flush();

			this.portalTitle.setVisible(false);
		}
	}

	@Override
	public void init()
	{
		final Pos2D center = InputHelper.getCenter();

		int i = 0;

		for (final GuiChoiceMenu menu : this.menus)
		{
			final GuiTexture choiceTab = new GuiTexture(
					Dim2D.build().pos(center).addY(-74).addX((-22 * this.menus.length) / 2).addX(i * 22).width(22).height(i == 0 || i == 2 ? 28 : 19).flush(),
					i == 0 ? CHOICE_TAB_LEFT_UNPRESSED : i == 2 ? CHOICE_TAB_RIGHT_UNPRESSED : CHOICE_TAB_UNPRESSED);

			final GuiText number = new GuiText(Dim2D.build().pos(8, 7).flush(), new Text(new TextComponentString(String.valueOf(i + 1)), 1.0F));

			choiceTab.addChildren(number);

			this.tabs[i] = choiceTab;

			this.addChildren(choiceTab);

			i++;
		}

		this.getCurrentTab().setResourceLocation(choicePageIndex == 0 ? CHOICE_TAB_LEFT : choicePageIndex == 2 ? CHOICE_TAB_RIGHT : CHOICE_TAB);

		this.portal = new GuiTexture(Dim2D.build().pos(center).addX(-75).width(24).height(24).center(true).flush(),
				this.mc.player.world.provider.getDimensionType() != WorldProviderOrbis.ORBIS ? PORTAL_TEXTURE : PORTAL_BACK_TEXTURE);

		this.addChildren(this.portal);

		this.left = new GuiTexture(
				Dim2D.build().pos(center).addY(54).addX(-23).width(22).height(16).flush(),
				LEFT_PLACEMENT_MODE_UNPRESSED);

		this.right = new GuiTexture(
				Dim2D.build().pos(center).addY(54).addX(-1).width(22).height(16).flush(),
				RIGHT_PLACEMENT_MODE_UNPRESSED);

		final GuiTexture genIcon = new GuiTexture(
				Dim2D.build().pos(center).addY(59).addX(-9).width(10).height(10).centerX(true).flush(),
				GENERATE_ICON);

		final GuiTexture scheduleIcon = new GuiTexture(
				Dim2D.build().pos(center).addY(59).addX(7).width(10).height(10).centerX(true).flush(),
				SCHEDULE_ICON);

		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.powers().isScheduling())
		{
			this.right.setResourceLocation(RIGHT_PLACEMENT_MODE);
		}
		else
		{
			this.left.setResourceLocation(LEFT_PLACEMENT_MODE);
		}

		this.portalTitle = new GuiText(Dim2D.build().center(true).pos(center).addY(-86).flush(), new Text(
				new TextComponentString(this.mc.player.world.provider.getDimensionType() != WorldProviderOrbis.ORBIS ? "Teleport to Orbis" : "Teleport Back"),
				1.0F));

		this.addChildren(this.left, this.right, genIcon, scheduleIcon);

		for (final GuiChoiceMenu menu : this.menus)
		{
			menu.setVisible(false);
			menu.setEnabled(false);

			this.addChildren(menu);
		}

		this.getCurrentMenu().setEnabled(true);
		this.getCurrentMenu().setVisible(true);

		this.addChildren(this.portalTitle);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		if (mouseButton == 0)
		{
			int i = 0;

			for (final GuiTexture tab : this.tabs)
			{
				if (InputHelper.isHovered(tab))
				{
					this.setCurrentPage(i);

					break;
				}

				i++;
			}

			if (InputHelper.isHovered(this.left))
			{
				this.left.setResourceLocation(LEFT_PLACEMENT_MODE);
				this.right.setResourceLocation(RIGHT_PLACEMENT_MODE_UNPRESSED);

				OrbisAPI.network().sendPacketToServer(new PacketSetScheduling(false));
				PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(false);
			}

			if (InputHelper.isHovered(this.right))
			{
				this.right.setResourceLocation(RIGHT_PLACEMENT_MODE);
				this.left.setResourceLocation(LEFT_PLACEMENT_MODE_UNPRESSED);

				OrbisAPI.network().sendPacketToServer(new PacketSetScheduling(true));
				PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(true);
			}

			if (InputHelper.isHovered(this.portal))
			{
				OrbisAPI.network().sendPacketToServer(new PacketTeleportOrbis());
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException
	{
		switch (keyCode)
		{
			case Keyboard.KEY_1:
				this.setCurrentPage(0);
				break;
			case Keyboard.KEY_2:
				this.setCurrentPage(1);
				break;
			case Keyboard.KEY_3:
				this.setCurrentPage(2);
				break;
			case Keyboard.KEY_4:
				this.setCurrentPage(3);
				break;
			case Keyboard.KEY_5:
				this.setCurrentPage(4);
				break;
			case Keyboard.KEY_6:
				this.setCurrentPage(5);
				break;
			case Keyboard.KEY_7:
				this.setCurrentPage(6);
				break;
			case Keyboard.KEY_8:
				this.setCurrentPage(7);
				break;
			case Keyboard.KEY_9:
				this.setCurrentPage(8);
				break;
		}

		super.keyTyped(typedChar, keyCode);
	}
}
