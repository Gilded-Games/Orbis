package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.IDataHolder;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintDataHolder;
import com.gildedgames.orbis.api.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.client.gui.data.list.IListNavigatorListener;
import com.gildedgames.orbis.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis.client.gui.util.GuiFactory;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.gui.util.list.GuiListViewer;
import com.gildedgames.orbis.client.gui.util.list.IListViewerListener;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.common.containers.slots.SlotBlueprintStacker;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import com.gildedgames.orbis.common.network.packets.gui.PacketBlueprintStackerGuiAddSlot;
import com.gildedgames.orbis.common.network.packets.gui.PacketBlueprintStackerGuiDisplaySlots;
import com.gildedgames.orbis.common.network.packets.gui.PacketBlueprintStackerGuiRemoveSlot;
import com.gildedgames.orbis.common.util.InputHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GuiCreateBlueprintStacker extends GuiFrame implements IListViewerListener, IListNavigatorListener<SlotBlueprintStacker>
{
	private static final ResourceLocation MENU = OrbisCore.getResource("blueprint_gui/height_variable_menu.png");

	private static final ResourceLocation SLOT = OrbisCore.getResource("blueprint_gui/height_slot.png");

	private ContainerLoadData container;

	private GuiListViewer<SlotBlueprintStacker, GuiTexture> stackViewer;

	private GuiAbstractButton createButton;

	public GuiCreateBlueprintStacker(GuiFrame parent, ContainerLoadData container)
	{
		super(parent, Dim2D.build().width(176).height(190).flush());

		this.container = container;
	}

	@Override
	public void init()
	{
		GuiTexture menu = new GuiTexture(Dim2D.build().width(176).height(192).flush(), MENU);

		this.stackViewer = new GuiListViewer<>
				(
						Dim2D.build().width(156).height(120).pos(Pos2D.build().add(10, 36).flush()).flush(),
						navigator -> navigator.getNodes().inverse().values().stream().max(Comparator.naturalOrder())
								.orElse(-1) + 1,
						this.container.getNavigator(),
						(p, n, i) -> new GuiTexture(Dim2D.build().pos(p).width(136).height(24).flush(), SLOT),
						i -> new SlotBlueprintStacker(this.container.stackerInventory, 40, i + 43, 6, 6 + ((i * 24) % 120)),
						24
				);

		this.stackViewer.listen(this);
		this.container.getNavigator().addListener(this);

		this.createButton = GuiFactory.createForgeButton();

		this.createButton.dim().mod().x(147).y(163).flush();

		this.addChildren(menu, this.stackViewer, this.createButton);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.createButton))
		{
			ItemStack topStack = this.container.getTopStackerSlot().getStack();
			ItemStack bottomStack = this.container.getBottomStackerSlot().getStack();

			if (!topStack.isEmpty() && !bottomStack.isEmpty())
			{
				IDataHolder<BlueprintData> top = new BlueprintDataHolder(ItemBlueprint.getBlueprint(topStack));
				IDataHolder<BlueprintData> bottom = new BlueprintDataHolder(ItemBlueprint.getBlueprint(bottomStack));

				if (top.get(null, null) == null)
				{
					top = ItemBlueprintPalette.getBlueprintPalette(topStack);
				}

				if (bottom.get(null, null) == null)
				{
					bottom = ItemBlueprintPalette.getBlueprintPalette(bottomStack);
				}

				List<IDataHolder<BlueprintData>> segments = Lists.newLinkedList();

				for (Map.Entry<Integer, SlotBlueprintStacker> entry : this.container.getNavigator().getNodes().entrySet())
				{
					ItemStack stack = entry.getValue().getStack();

					if (!stack.isEmpty())
					{
						BlueprintData bp = ItemBlueprint.getBlueprint(stack);

						if (bp != null)
						{
							segments.add(new BlueprintDataHolder(bp));
							continue;
						}

						IDataHolder<BlueprintData> palette = ItemBlueprintPalette.getBlueprintPalette(stack);

						if (palette != null)
						{
							segments.add(palette);
						}
					}
				}

				BlueprintStackerData stacker = new BlueprintStackerData(top, bottom, segments.toArray(new IDataHolder[segments.size()]));

				Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this.getPrevFrame(), stacker, BlueprintStackerData.EXTENSION));
			}
		}
	}

	private void displaySlotsToServer()
	{
		int[] slots = new int[this.stackViewer.getVisibleNodes().size()];

		for (int i = 0; i < this.stackViewer.getVisibleNodes().size(); i++)
		{
			SlotBlueprintStacker slot = this.stackViewer.getVisibleNodes().get(i);
			int id = this.stackViewer.findPosition(slot);

			slots[i] = id;
		}

		OrbisAPI.network().sendPacketToServer(new PacketBlueprintStackerGuiDisplaySlots(slots));
	}

	@Override
	public void onScroll(int currentScroll, int newScroll)
	{
		this.container.display(this.stackViewer.getVisibleNodes());

		this.displaySlotsToServer();
	}

	@Override
	public void onRemoveNode(SlotBlueprintStacker node, int index)
	{
		this.container.stackerInventory.remove(index + 43);

		OrbisAPI.network()
				.sendPacketToServer(new PacketBlueprintStackerGuiRemoveSlot(index));

		this.container.removeStackerSlot(node);
		this.container.display(this.stackViewer.getVisibleNodes());

		this.displaySlotsToServer();
	}

	@Override
	public void onAddNode(SlotBlueprintStacker node, int index, boolean newNode)
	{
		this.container.stackerInventory.expand(index + 43);
		this.container.addStackerSlot(node);
		OrbisAPI.network().sendPacketToServer(new PacketBlueprintStackerGuiAddSlot(index + 43, node.xPos, node.yPos));
	}

	@Override
	public void onNodeClicked(SlotBlueprintStacker node, int index)
	{

	}
}
