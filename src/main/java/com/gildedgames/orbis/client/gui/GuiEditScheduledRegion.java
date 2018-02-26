package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.api.inventory.InventorySpawnEggs;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.util.*;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerScheduleRegion;
import com.gildedgames.orbis.common.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiEditScheduledRegion extends GuiFrame
{

	private static final ResourceLocation MATRIX_ICON = OrbisCore.getResource("filter_gui/filter_matrix.png");

	private static final ResourceLocation INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private final ScheduleRegion scheduleRegion;

	private final ContainerScheduleRegion container;

	private GuiInput nameInput;

	private GuiButtonVanilla saveButton, closeButton;

	public GuiEditScheduledRegion(PlayerOrbis playerOrbis, ScheduleRegion scheduleRegion)
	{
		super(Dim2D.flush(), new ContainerScheduleRegion(playerOrbis, new InventorySpawnEggs(null)));

		this.container = (ContainerScheduleRegion) this.inventorySlots;
		playerOrbis.getEntity().openContainer = this.inventorySlots;

		this.scheduleRegion = scheduleRegion;

		this.allowUserInput = true;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.guiLeft = this.width / 2 - 94;
		this.guiTop = this.height / 2 - 83;

		this.xSize = 179 * 2;
		this.ySize = 169;
	}

	@Override
	public void init()
	{
		final Pos2D center = Pos2D.flush((this.width / 2), this.height / 2);

		final int yOffset = -70;
		int yOffsetInput = 15;
		int xOffsetInput = 47;

		GuiText title = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(-25).addX(-32).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush(),
				new Text(new TextComponentString("Trigger ID:"), 1.0F));

		this.nameInput = new GuiInput(Dim2D.build().center(true).width(110).height(20).pos(center).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.nameInput.getInner().setText(this.scheduleRegion.getTriggerID());

		this.saveButton = new GuiButtonVanilla(
				Dim2D.build().center(true).width(50).height(20).pos(center).addY(30).addX(-30).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.saveButton.getInner().displayString = "Save";

		this.closeButton = new GuiButtonVanilla(
				Dim2D.build().center(true).width(50).height(20).pos(center).addY(30).addX(30).addX(xOffsetInput).addY(yOffset + yOffsetInput).flush());

		this.closeButton.getInner().displayString = "Close";

		GuiTexture matrix = new GuiTexture(Dim2D.build().width(85).height(105).pos(center).addX(-101).addY(-40 + yOffset).flush(), MATRIX_ICON);
		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).pos(center).centerX(true).addY(70 + yOffset).flush(), INVENTORY);

		GuiText spawnEggTitle = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(-29).addX(-88).addY(yOffset).flush(),
				new Text(new TextComponentString("Spawn Eggs"), 1.0F));

		this.addChildren(title, this.nameInput, this.saveButton, this.closeButton, matrix, inventory, spawnEggTitle);
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks)
	{
		this.drawWorldBackground(0);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			this.scheduleRegion.setTriggerId(this.nameInput.getInner().getText());
			//TODO: SAVE
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
	{
	}

	@Override
	public void drawDefaultBackground()
	{
	}
}
