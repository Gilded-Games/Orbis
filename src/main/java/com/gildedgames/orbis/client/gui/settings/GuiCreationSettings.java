package com.gildedgames.orbis.client.gui.settings;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerCreationSettings;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.variables.displays.GuiTickBox;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class GuiCreationSettings extends GuiViewer
{
	private static final ResourceLocation INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private static final ResourceLocation TITLE_BAR_TEXTURE = OrbisCore.getResource("orbis_settings/title_bar.png");

	private final ContainerCreationSettings container;

	private GuiText creationSettingsTitle, placeAirBlocksTitle;

	private GuiTexture titleBar;

	private GuiTickBox placeAirBlocks;

	private PlayerOrbis playerOrbis;

	public GuiCreationSettings(final EntityPlayer player)
	{
		super(new GuiElement(Dim2D.flush(), false), null,
				new ContainerCreationSettings(player.inventory));

		this.playerOrbis = PlayerOrbis.get(player);

		this.container = (ContainerCreationSettings) this.inventorySlots;
	}

	@Override
	public void initContainerSize()
	{
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = ((this.height - this.ySize) / 2) - 38;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		this.playerOrbis.getCreationSettings().setPlacesAirBlocks(this.placeAirBlocks.isTicked());
	}

	@Override
	public void build(IGuiContext context)
	{
		Pos2D center = InputHelper.getCenter();

		this.titleBar = new GuiTexture(Dim2D.build().width(154).height(30).center(true).snapToIntegers(true).pos(center).addY(-45).flush(),
				TITLE_BAR_TEXTURE);

		this.creationSettingsTitle = new GuiText(Dim2D.build().pos(center).centerX(true).addY(-50).flush(),
				new Text(new TextComponentTranslation("orbis.gui.creation_settings"), 1.0F));

		context.addChildren(this.titleBar);
		context.addChildren(this.creationSettingsTitle);

		this.placeAirBlocksTitle = new GuiText(Dim2D.build().pos(center).centerX(true).addY(-20).flush(),
				new Text(new TextComponentTranslation("orbis.gui.place_air_blocks"), 1.0F));

		this.placeAirBlocks = new GuiTickBox(center.clone().addX(-62).addY(-23).flush(), this.playerOrbis.getCreationSettings().placesAirBlocks());

		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).center(true).snapToIntegers(true).pos(center).addY(45).flush(), INVENTORY);

		context.addChildren(inventory, this.placeAirBlocks, this.placeAirBlocksTitle);
	}

	@Override
	public void drawElements()
	{
		super.drawElements();
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
