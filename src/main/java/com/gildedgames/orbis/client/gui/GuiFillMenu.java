package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.schedules.FilterOptions;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.util.*;
import com.gildedgames.orbis.client.gui.util.vanilla.GuiContainerCreativePublic;
import com.gildedgames.orbis.client.gui.util.vanilla.GuiFrameCreative;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.slots.SlotForge;
import com.gildedgames.orbis.common.items.ItemBlockPalette;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.network.packets.PacketSetFilterOptions;
import com.gildedgames.orbis.common.util.InputHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.util.List;

public class GuiFillMenu extends GuiFrameCreative
{
	private static final ResourceLocation MATRIX_ICON = OrbisCore.getResource("filter_gui/filter_matrix.png");

	private static final ResourceLocation FLOW_ICON = OrbisCore.getResource("filter_gui/flow_icon.png");

	private final ContainerFillMenu container;

	private GuiAbstractButton forgeButton;

	private GuiTexture matrix, flow;

	private GuiText combineTitle;

	private GuiInputSlider noise;

	private GuiTickBox choosesPerBlock;

	private PlayerOrbis playerOrbis;

	public GuiFillMenu(final EntityPlayer player, final IInventory forgeInventory)
	{
		super(player);

		this.playerOrbis = PlayerOrbis.get(player);

		this.setExtraSlots(16);

		this.container = new ContainerFillMenu(player, forgeInventory, this);

		this.inventorySlots = this.container;
		player.openContainer = this.inventorySlots;
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

		this.playerOrbis.powers().getFillPower().getFilterOptions().setChoosesPerBlock(this.choosesPerBlock.isTicked())
				.setEdgeNoise(this.noise.getSliderValue());

		OrbisAPI.network().sendPacketToServer(
				new PacketSetFilterOptions(new FilterOptions().setChoosesPerBlock(this.choosesPerBlock.isTicked()).setEdgeNoise(this.noise.getSliderValue())));
	}

	@Override
	public void init()
	{
		Pos2D center = Pos2D.flush((this.width / 2) + 100, this.height / 2);

		this.forgeButton = GuiFactory.createForgeButton();

		this.forgeButton.dim().mod().pos(center).center(true).addY(72 - 6).addX(60).flush();

		this.matrix = new GuiTexture(Dim2D.build().width(85).height(105).center(true).pos(center).addX(60).addY(-15).flush(), MATRIX_ICON);
		this.flow = new GuiTexture(Dim2D.build().width(8).height(14).center(true).pos(center).addX(60).addY(52 - 6).flush(), FLOW_ICON);

		this.combineTitle = new GuiText(Dim2D.build().pos(center).centerX(true).addX(60).addY(-49 - 9).flush(),
				new Text(new TextComponentString("Combine"), 1.0F));

		this.addChildren(this.matrix);
		this.addChildren(this.flow);
		this.addChildren(this.combineTitle);

		this.addChildren(this.forgeButton);

		center = InputHelper.getCenter();

		GuiText noiseTitle = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(-41).addX(-200).flush(),
				new Text(new TextComponentString("Edge Noise:"), 1.0F));

		this.noise = new GuiInputSlider(Dim2D.build().height(20).width(60).flush(), 0, 100, 1.0F);

		this.noise.dim().mod().width(80).pos(center).addX(-200).addY(-30).flush();

		this.noise.setSliderValue(this.playerOrbis.powers().getFillPower().getFilterOptions().getEdgeNoise());

		GuiText chooseTitle = new GuiText(Dim2D.build().width(140).height(20).pos(center).addY(0).addX(-200).flush(),
				new Text(new TextComponentString("Chooses Per Block:"), 1.0F));

		this.choosesPerBlock = new GuiTickBox(center.clone().addX(-200).addY(11).flush(),
				this.playerOrbis.powers().getFillPower().getFilterOptions().choosesPerBlock());

		this.addChildren(this.noise, noiseTitle, this.choosesPerBlock, chooseTitle);
	}

	@Override
	public void draw()
	{
		this.forgeButton.setEnabled(this.getItemStacksInForge().size() >= 2);

		this.forgeButton.setVisible(this.getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex());
		this.matrix.setVisible(this.getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex());
		this.flow.setVisible(this.getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex());
		this.combineTitle.setVisible(this.getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex());
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.forgeButton) && mouseButton == 0)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.block_palette);

			ItemBlockPalette.setFilterLayer(stack, BlockFilterHelper.createFillLayer(this.getItemStacksInForge()));

			Minecraft.getMinecraft().player.inventory.setItemStack(stack);
		}
	}

	public static class ContainerFillMenu extends ContainerCreativePublic
	{

		public SlotForge[] slots;

		public ContainerFillMenu(final EntityPlayer player, final IInventory forgeInventory, final GuiContainerCreativePublic gui)
		{
			super(player, gui);

			this.slots = new SlotForge[4 * 4];

			final int indexOffset = 55;

			for (int i = 0; i < 4; ++i)
			{
				for (int j = 0; j < 4; ++j)
				{
					final SlotForge slot = new SlotForge(forgeInventory, indexOffset, indexOffset + (i * 4 + j), 222 + j * 18, 27 + i * 18);

					this.addSlotToContainer(slot);

					this.slots[i * 4 + j] = slot;
				}
			}
		}

	}
}
