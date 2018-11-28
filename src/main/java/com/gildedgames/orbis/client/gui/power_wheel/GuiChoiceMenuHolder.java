package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import com.gildedgames.orbis.common.network.packets.PacketSetScheduling;
import com.gildedgames.orbis.common.network.packets.PacketTeleportOrbis;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.util.InputHelper;
import com.hrznstudio.roadworks.api.RoadworksAPI;
import com.hrznstudio.roadworks.api.input.Controller;
import com.hrznstudio.roadworks.api.input.ControllerEvent;
import com.hrznstudio.roadworks.api.input.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = OrbisCore.MOD_ID)
public class GuiChoiceMenuHolder extends GuiViewer
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

	private final static ResourceLocation ORBIS_DIM_SETTINGS_TEXTURE = OrbisCore.getResource("godmode/overlay/orbis_dim_settings.png");

	private final static ResourceLocation SETTINGS_TEXTURE = OrbisCore.getResource("godmode/overlay/settings.png");

	private static int choicePageIndex;

	private final GuiChoiceMenu[] menus;

	private final GuiTexture[] tabs;

	private GuiTexture left, right, portal, orbisDimSettings, settings;

	private GuiText customTitle;

	public GuiChoiceMenuHolder(final GuiChoiceMenu... menus)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(false);

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
			menu.state().setVisible(false);
			menu.state().setEnabled(false);
		}

		int i = 0;

		for (final GuiTexture tab : this.tabs)
		{
			tab.setResourceLocation(i == 0 ? CHOICE_TAB_LEFT_UNPRESSED : i == 2 ? CHOICE_TAB_RIGHT_UNPRESSED : CHOICE_TAB_UNPRESSED);

			i++;
		}

		this.getCurrentMenu().state().setEnabled(true);
		this.getCurrentMenu().state().setVisible(true);

		this.getCurrentTab().setResourceLocation(choicePageIndex == 0 ? CHOICE_TAB_LEFT : choicePageIndex == 2 ? CHOICE_TAB_RIGHT : CHOICE_TAB);
	}

	@Override
	public void onMouseWheel(final int state)
	{
		super.onMouseWheel(state);

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
	public void drawElements()
	{
		this.customTitle.setText(Text.EMPTY);

		super.drawElements();

		if (this.portal.state().isHoveredAndTopElement())
		{
			this.portal.dim().mod().scale(1.1F).flush();
			this.customTitle.setText(new Text(new TextComponentString(
					this.mc.player.world.provider.getDimensionType() != WorldProviderOrbis.ORBIS ? "Teleport to Orbis" : "Teleport Back"), 1.0F));
		}
		else
		{
			this.portal.dim().mod().scale(1.0F).flush();
		}

		if (this.orbisDimSettings.state().isHoveredAndTopElement())
		{
			this.orbisDimSettings.dim().mod().scale(1.1F).flush();
			this.customTitle.setText(new Text(new TextComponentString("Orbis Settings"), 1.0F));
		}
		else
		{
			this.orbisDimSettings.dim().mod().scale(1.0F).flush();
		}

		if (this.settings.state().isHoveredAndTopElement())
		{
			this.settings.dim().mod().scale(1.1F).flush();
			this.customTitle.setText(new Text(new TextComponentString("Creation Settings"), 1.0F));
		}
		else
		{
			this.settings.dim().mod().scale(1.0F).flush();
		}
	}

	@Override
	public void build(IGuiContext context)
	{
		final Pos2D center = InputHelper.getCenter();

		int i = 0;

		for (final GuiChoiceMenu menu : this.menus)
		{
			final GuiTexture choiceTab = new GuiTexture(
					Dim2D.build().pos(center).addY(-74).addX((-22 * this.menus.length) / 2).addX(i * 22).width(22).height(i == 0 || i == 2 ? 28 : 19).flush(),
					i == 0 ? CHOICE_TAB_LEFT_UNPRESSED : i == 2 ? CHOICE_TAB_RIGHT_UNPRESSED : CHOICE_TAB_UNPRESSED);

			final GuiText number = new GuiText(Dim2D.build().pos(8, 7).flush(), new Text(new TextComponentString(String.valueOf(i + 1)), 1.0F));

			choiceTab.build(this);

			choiceTab.context().addChildren(number);
			choiceTab.state().setCanBeTopHoverElement(true);

			this.tabs[i] = choiceTab;

			context.addChildren(choiceTab);

			i++;
		}

		this.getCurrentTab().setResourceLocation(choicePageIndex == 0 ? CHOICE_TAB_LEFT : choicePageIndex == 2 ? CHOICE_TAB_RIGHT : CHOICE_TAB);

		this.portal = new GuiTexture(Dim2D.build().pos(center).addX(-75).width(24).height(24).center(true).flush(),
				this.mc.player.world.provider.getDimensionType() != WorldProviderOrbis.ORBIS ? PORTAL_TEXTURE : PORTAL_BACK_TEXTURE);

		this.orbisDimSettings = new GuiTexture(Dim2D.build().pos(center).addX(-68).addY(-25).width(14).height(14).center(true).flush(),
				ORBIS_DIM_SETTINGS_TEXTURE);

		this.settings = new GuiTexture(Dim2D.build().pos(center).addX(75).addY(0).width(14).height(14).center(true).flush(),
				SETTINGS_TEXTURE);

		if (this.mc.player.world.provider.getDimensionType() == WorldProviderOrbis.ORBIS)
		{
			context.addChildren(this.orbisDimSettings);
		}

		context.addChildren(this.portal, this.settings);

		this.portal.state().setCanBeTopHoverElement(true);
		this.orbisDimSettings.state().setCanBeTopHoverElement(true);
		this.settings.state().setCanBeTopHoverElement(true);

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

		this.customTitle = new GuiText(Dim2D.build().center(true).pos(center).addY(-86).flush(), Text.EMPTY);

		if (OrbisCore.CONFIG.useExperimentalFeatures())
		{
			context.addChildren(this.left, this.right, genIcon, scheduleIcon);
		}

		for (final GuiChoiceMenu menu : this.menus)
		{
			menu.state().setVisible(false);
			menu.state().setEnabled(false);

			context.addChildren(menu);
		}

		this.getCurrentMenu().state().setEnabled(true);
		this.getCurrentMenu().state().setVisible(true);

		context.addChildren(this.customTitle);

		this.left.state().setCanBeTopHoverElement(true);
		this.right.state().setCanBeTopHoverElement(true);
	}

	@SubscribeEvent
	public static void controllerButtonEvent(ControllerEvent.Button event)
	{
		GuiChoiceMenuHolder gui = null;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChoiceMenuHolder)
		{
			gui = (GuiChoiceMenuHolder) Minecraft.getMinecraft().currentScreen;
		}
		if (gui == null)
			return;
		ControllerManager manager = RoadworksAPI.getInstance().getControllerManager();
		if (manager.getActiveController().isPresent())
		{
			Controller controller = manager.getActiveController().get();
			if (event.getController() == controller)
			{
				if (event.isPressed())
				{
					int currentIndex = choicePageIndex;
					int maxIndex = gui.tabs.length - 1;
					int minIndex = 0;
					int nextIndex = currentIndex;
					if (event.getButton() == Controller.Button.BUMPER_RIGHT)
					{
						nextIndex++;
						if (currentIndex > maxIndex)
							nextIndex = minIndex;
					} else if (event.getButton() == Controller.Button.BUMPER_LEFT)
					{
						nextIndex--;
						if (currentIndex < minIndex)
							nextIndex = maxIndex;
					}
					if (nextIndex != currentIndex)
					{
						gui.setCurrentPage(nextIndex);
					}

					if (event.getButton() == Controller.Button.MENU)
					{
						OrbisCore.network().sendPacketToServer(new PacketTeleportOrbis());
					}

					if (event.getButton() == Controller.Button.DPAD_RIGHT)
					{
						gui.right.setResourceLocation(RIGHT_PLACEMENT_MODE);
						gui.left.setResourceLocation(LEFT_PLACEMENT_MODE_UNPRESSED);

						OrbisCore.network().sendPacketToServer(new PacketSetScheduling(true));
						PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(true);
					}
					if (event.getButton() == Controller.Button.DPAD_LEFT)
					{
						gui.left.setResourceLocation(LEFT_PLACEMENT_MODE);
						gui.right.setResourceLocation(RIGHT_PLACEMENT_MODE_UNPRESSED);

						OrbisCore.network().sendPacketToServer(new PacketSetScheduling(false));
						PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(false);
					}
					if (event.getButton() == Controller.Button.X)
					{
						EntityPlayer player = Minecraft.getMinecraft().player;
						if (player.world.provider.getDimensionType() == WorldProviderOrbis.ORBIS)
						{
							OrbisCore.network().sendPacketToServer(
									new PacketOpenGui(OrbisGuiHandler.ORBIS_SETTINGS, player.getPosition().getX(), player.getPosition().getY(),
											player.getPosition().getZ()));
						}
					}
					if (event.getButton() == Controller.Button.B)
					{
						EntityPlayer player = Minecraft.getMinecraft().player;

						OrbisCore.network().sendPacketToServer(
								new PacketOpenGui(OrbisGuiHandler.CREATION_SETTINGS, player.getPosition().getX(), player.getPosition().getY(),
										player.getPosition().getZ()));
					}
				}
			}
		}
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		if (RoadworksAPI.isAvailable() && RoadworksAPI.getInstance().getControllerManager().getActiveController().isPresent())
			return;
		if (mouseButton == 0)
		{
			int i = 0;

			for (final GuiTexture tab : this.tabs)
			{
				if (tab.state().isHoveredAndTopElement())
				{
					this.setCurrentPage(i);

					break;
				}

				i++;
			}

			if (this.left.state().isHoveredAndTopElement())
			{
				this.left.setResourceLocation(LEFT_PLACEMENT_MODE);
				this.right.setResourceLocation(RIGHT_PLACEMENT_MODE_UNPRESSED);

				OrbisCore.network().sendPacketToServer(new PacketSetScheduling(false));
				PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(false);
			}

			if (this.right.state().isHoveredAndTopElement())
			{
				this.right.setResourceLocation(RIGHT_PLACEMENT_MODE);
				this.left.setResourceLocation(LEFT_PLACEMENT_MODE_UNPRESSED);

				OrbisCore.network().sendPacketToServer(new PacketSetScheduling(true));
				PlayerOrbis.get(Minecraft.getMinecraft().player).powers().setScheduling(true);
			}

			if (this.portal.state().isHoveredAndTopElement())
			{
				OrbisCore.network().sendPacketToServer(new PacketTeleportOrbis());
			}

			if (this.orbisDimSettings.state().isHoveredAndTopElement())
			{
				EntityPlayer player = Minecraft.getMinecraft().player;

				OrbisCore.network().sendPacketToServer(
						new PacketOpenGui(OrbisGuiHandler.ORBIS_SETTINGS, player.getPosition().getX(), player.getPosition().getY(),
								player.getPosition().getZ()));
			}

			if (this.settings.state().isHoveredAndTopElement())
			{
				EntityPlayer player = Minecraft.getMinecraft().player;

				OrbisCore.network().sendPacketToServer(
						new PacketOpenGui(OrbisGuiHandler.CREATION_SETTINGS, player.getPosition().getX(), player.getPosition().getY(),
								player.getPosition().getZ()));
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
