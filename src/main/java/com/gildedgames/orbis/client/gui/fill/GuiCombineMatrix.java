package com.gildedgames.orbis.client.gui.fill;

import com.gildedgames.orbis.client.gui.util.GuiFactoryOrbis;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerCombineMatrix;
import com.gildedgames.orbis.common.containers.slots.SlotForge;
import com.gildedgames.orbis.common.items.ItemBlockPalette;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetFilterOptions;
import com.gildedgames.orbis.common.network.packets.PacketSetItemStack;
import com.gildedgames.orbis.lib.block.BlockFilterHelper;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis.lib.client.gui.util.GuiInputSlider;
import com.gildedgames.orbis.lib.client.gui.util.GuiText;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.core.variables.displays.GuiTickBox;
import com.gildedgames.orbis.lib.util.InputHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.List;

public class GuiCombineMatrix extends GuiViewer
{
	private static final ResourceLocation INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private static final ResourceLocation MATRIX_ICON = OrbisCore.getResource("filter_gui/filter_matrix.png");

	private static final ResourceLocation FLOW_ICON = OrbisCore.getResource("filter_gui/flow_icon.png");

	private final ContainerCombineMatrix container;

	private GuiAbstractButton forgeButton;

	private GuiTexture matrix, flow;

	private GuiText combineTitle;

	private GuiInputSlider noise;

	private GuiTickBox choosesPerBlock;

	private PlayerOrbis playerOrbis;

	private GuiButtonVanilla back;

	public GuiCombineMatrix(final EntityPlayer player)
	{
		super(new GuiElement(Dim2D.flush(), false), null,
				new ContainerCombineMatrix(player.inventory, PlayerOrbis.get(player).powers().getFillPower().getForgeInventory()));

		this.playerOrbis = PlayerOrbis.get(player);

		this.container = (ContainerCombineMatrix) this.inventorySlots;
	}

	@Override
	public void initContainerSize()
	{
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = ((this.height - this.ySize) / 2) - 38;
	}

	private List<ItemStack> getItemStacksInForge()
	{
		final List<ItemStack> stacks = Lists.newArrayList();

		for (int i = 0; i < this.container.slots.length; i++)
		{
			final SlotForge slot = this.container.slots[i];

			if (slot.getStack() != null && !slot.getStack().isEmpty())
			{
				stacks.add(slot.getStack());
			}
		}

		return stacks;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		this.playerOrbis.powers().getFillPower().getFilterOptions().getChoosesPerBlockVar().setData(this.choosesPerBlock.isTicked());
		this.playerOrbis.powers().getFillPower().getFilterOptions().getEdgeNoiseVar().setData(this.noise.getSliderValue());

		OrbisCore.network().sendPacketToServer(
				new PacketSetFilterOptions(this.playerOrbis.powers().getFillPower().getFilterOptions()));
	}

	@Override
	public void build(IGuiContext context)
	{
		Pos2D center = Pos2D.flush(MathHelper.floor((this.width / 2) + 100), MathHelper.floor(this.height / 2));

		this.forgeButton = GuiFactoryOrbis.createForgeButton();

		this.forgeButton.dim().mod().pos(center).center(true).addY(72 - 6).addX(60).flush();

		this.matrix = new GuiTexture(Dim2D.build().width(85).height(105).center(true).snapToIntegers(true).pos(center).addX(60).addY(-15).flush(), MATRIX_ICON);
		this.flow = new GuiTexture(Dim2D.build().width(8).height(14).center(true).snapToIntegers(true).pos(center).addX(60).addY(52 - 6).flush(), FLOW_ICON);

		this.combineTitle = new GuiText(Dim2D.build().pos(center).centerX(true).addX(60).addY(-49 - 9).flush(),
				new Text(new TextComponentTranslation("orbis.gui.combine"), 1.0F));

		context.addChildren(this.matrix);
		context.addChildren(this.flow);
		context.addChildren(this.combineTitle);

		context.addChildren(this.forgeButton);

		center = InputHelper.getCenter();

		GuiText noiseTitle = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(-41).addX(-200).flush(),
				new Text(new TextComponentString("Edge Noise:"), 1.0F));

		this.noise = new GuiInputSlider(Dim2D.build().height(20).width(60).flush(), 0, 100, 1.0F);

		this.noise.dim().mod().width(80).pos(center).addX(-200).addY(-30).flush();

		this.noise.setSliderValue(this.playerOrbis.powers().getFillPower().getFilterOptions().getEdgeNoiseVar().getData());

		GuiText fillingOptions = new GuiText(Dim2D.build().width(140).height(20).pos(center).center(true).addY(-85).addX(-12).flush(),
				new Text(new TextComponentTranslation("orbis.gui.fillingOptions"), 1.0F));

		GuiText chooseTitle = new GuiText(Dim2D.build().width(140).height(20).pos(center).center(true).addY(-65).addX(0).flush(),
				new Text(new TextComponentTranslation("orbis.gui.choosePerBlock"), 1.0F));

		this.choosesPerBlock = new GuiTickBox(center.clone().addX(-68).addY(-73).flush(),
				this.playerOrbis.powers().getFillPower().getFilterOptions().getChoosesPerBlockVar().getData());

		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).center(true).snapToIntegers(true).pos(center).flush(), INVENTORY);

		this.back = new GuiButtonVanilla(Dim2D.build().pos(center).addY(65).center(true).width(80).height(20).flush());

		this.back.getInner().displayString = I18n.format("orbis.gui.back");

		this.back.state().setCanBeTopHoverElement(true);

		context.addChildren(this.choosesPerBlock, fillingOptions, chooseTitle, inventory, this.back);
	}

	@Override
	public void drawElements()
	{
		super.drawElements();

		this.forgeButton.state().setEnabled(this.getItemStacksInForge().size() >= 2);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.forgeButton.state().isEnabled() && this.forgeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.block_palette);

			ItemBlockPalette.setFilterLayer(stack, BlockFilterHelper.createFillLayer(this.getItemStacksInForge()));

			OrbisCore.network().sendPacketToServer(new PacketSetItemStack(stack));
			Minecraft.getMinecraft().player.inventory.setItemStack(stack);
		}

		if (this.back.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(this.mc.player));
		}
	}

	public GuiAbstractButton getForgeButton()
	{
		return forgeButton;
	}

	public GuiTexture getMatrix()
	{
		return matrix;
	}

	public GuiTexture getFlow()
	{
		return flow;
	}
}
