package com.gildedgames.orbis.client.gui.settings;

import com.gildedgames.orbis.client.model.ModelOrbisFloor;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerOrbisSettings;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiInput;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.IGuiInputListener;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.variables.displays.GuiTickBox;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class GuiOrbisSettings extends GuiViewer implements IGuiInputListener
{
	private static final ResourceLocation INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private static final ResourceLocation FLOOR_VISUALS_ICON = OrbisCore.getResource("orbis_settings/floor_visuals_gui.png");

	public static String FLOOR_COLOR = "FFFFFF";

	public static IBlockState FLOOR_BLOCK;

	private final ContainerOrbisSettings container;

	private GuiTexture floor_visuals_back;

	private GuiText changeFloorVisualTitle;

	private GuiTickBox block, color;

	private PlayerOrbis playerOrbis;

	private GuiInput colorInput;

	public GuiOrbisSettings(final EntityPlayer player)
	{
		super(new GuiElement(Dim2D.flush(), false), null,
				new ContainerOrbisSettings(player.inventory, PlayerOrbis.get(player).getOrbisSettingsInventory()));

		this.playerOrbis = PlayerOrbis.get(player);

		this.container = (ContainerOrbisSettings) this.inventorySlots;
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

		FLOOR_COLOR = this.colorInput.getInner().getText();
	}

	@Override
	public void build(IGuiContext context)
	{
		Pos2D center = InputHelper.getCenter();

		this.floor_visuals_back = new GuiTexture(Dim2D.build().width(154).height(69).center(true).snapToIntegers(true).pos(center).addY(-45).flush(),
				FLOOR_VISUALS_ICON);

		this.changeFloorVisualTitle = new GuiText(Dim2D.build().pos(center).centerX(true).addY(-70).flush(),
				new Text(new TextComponentTranslation("orbis.gui.change_floor_visuals"), 1.0F));

		context.addChildren(this.floor_visuals_back);
		context.addChildren(this.changeFloorVisualTitle);

		this.block = new GuiTickBox(center.clone().addX(-72).addY(-36).flush(), ModelOrbisFloor.useBlock);
		this.color = new GuiTickBox(center.clone().addX(-20).addY(-36).flush(), !ModelOrbisFloor.useBlock);

		this.block.listenOnPress((ticked) ->
		{
			if (!ticked)
			{
				this.block.setTicked(true);
				return;
			}

			this.color.setTicked(false);

			ModelOrbisFloor.useBlock = true;
		});

		this.color.listenOnPress((ticked) ->
		{
			if (!ticked)
			{
				this.color.setTicked(true);
				return;
			}

			this.block.setTicked(false);

			ModelOrbisFloor.useBlock = false;
		});

		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).center(true).snapToIntegers(true).pos(center).addY(45).flush(), INVENTORY);

		this.colorInput = new GuiInput(Dim2D.build().width(69).height(20).pos(center).addX(0).addY(-39).flush());

		this.colorInput.getInner().setText(FLOOR_COLOR);

		this.colorInput.listen(this);

		context.addChildren(inventory, this.block, this.color, this.colorInput);
	}

	@Override
	public void drawElements()
	{
		super.drawElements();

		if (this.container.orbisFloorSlot.getStack().getItem() instanceof ItemBlock)
		{
			FLOOR_BLOCK = ((ItemBlock) this.container.orbisFloorSlot.getStack().getItem()).getBlock()
					.getStateFromMeta(this.container.orbisFloorSlot.getStack().getItemDamage());
		}
		else
		{
			FLOOR_BLOCK = null;
		}
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onPressEnter()
	{
		FLOOR_COLOR = this.colorInput.getInner().getText();
	}
}
