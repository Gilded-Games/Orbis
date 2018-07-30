package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.util.GuiTab;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerEditBlueprintPostGen;
import com.gildedgames.orbis.common.containers.SlotGroup;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintAddPostGenReplaceLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintPostgenReplaceLayerChanges;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintRemovePostGenReplaceLayer;
import com.gildedgames.orbis.common.network.packets.gui.PacketPostGenAddLayer;
import com.gildedgames.orbis.common.network.packets.gui.PacketPostGenDisplayLayers;
import com.gildedgames.orbis.common.network.packets.gui.PacketPostGenRemoveLayer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.data.list.IListNavigatorListener;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.gui.util.list.GuiListViewer;
import com.gildedgames.orbis_api.client.gui.util.list.IListViewerListener;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.schedules.PostGenReplaceLayer;
import com.gildedgames.orbis_api.util.InputHelper;
import com.gildedgames.orbis_api.util.mc.SlotHashed;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GuiEditBlueprintPostGen extends GuiViewer implements IListViewerListener, IListNavigatorListener<SlotGroup>
{
	private static final ResourceLocation VIEWER_BACKDROP = OrbisCore.getResource("list/list_viewer.png");

	private static final ResourceLocation LAYERS_ICON = OrbisCore.getResource("blueprint_gui/layers_icon.png");

	private static final ResourceLocation POST_GEN_ICON = OrbisCore.getResource("blueprint_gui/post_gen_icon.png");

	private static final ResourceLocation BLUEPRINT_INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private final Blueprint blueprint;

	private GuiButtonVanilla saveButton, closeButton;

	private GuiTab layerTab, postGenTab;

	private GuiListViewer<SlotGroup, ReplaceLayerSlot> layerViewer;

	private ContainerEditBlueprintPostGen container;

	private PlayerOrbis playerOrbis;

	public GuiEditBlueprintPostGen(GuiViewer prevFrame, final Blueprint blueprint)
	{
		super(new GuiElement(Dim2D.flush(), false), prevFrame);

		this.setDrawDefaultBackground(true);
		this.blueprint = blueprint;

		this.playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		this.container = new ContainerEditBlueprintPostGen(this.playerOrbis, blueprint);

		this.inventorySlots = this.container;
		this.playerOrbis.getEntity().openContainer = this.inventorySlots;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		for (Map.Entry<Integer, SlotGroup> entry : this.layerViewer.getNavigator().getNodes().entrySet())
		{
			int layerId = entry.getKey();
			SlotGroup pair = entry.getValue();

			NetworkingOrbis.network().sendPacketToServer(
					new PacketBlueprintPostgenReplaceLayerChanges(this.blueprint, layerId, pair.getSlots().getLeft().getStack(),
							pair.getSlots().getRight().getStack()));
		}
	}

	@Override
	public void initContainerSize()
	{
		this.guiLeft = this.width / 2 - 122 - (176 / 2);
		this.guiTop = this.height / 2 - (147 / 2);

		this.xSize = 179 * 2;
		this.ySize = 169;
	}

	@Override
	public void build(IGuiContext context)
	{
		Pos2D center = InputHelper.getCenter();

		this.layerTab = new GuiTab(Dim2D.build().x(center.x()).addX(-11).centerX(true).flush(),
				new GuiTexture(Dim2D.build().width(16).height(16).flush(), LAYERS_ICON),
				() -> this.mc.displayGuiScreen(this.getPreviousViewer().getActualScreen()));
		this.postGenTab = new GuiTab(Dim2D.build().x(center.x()).addX(11).centerX(true).flush(),
				new GuiTexture(Dim2D.build().width(16).height(16).flush(), POST_GEN_ICON),
				() -> {
				});

		this.postGenTab.setPressed(true);

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(75).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.closeButton.getInner().displayString = "Close";

		context.addChildren(this.saveButton);
		context.addChildren(this.closeButton);

		final GuiTexture backdrop = new GuiTexture(Dim2D.build().pos(Pos2D.build().add(20, 45).flush()).width(176).flush(), VIEWER_BACKDROP);

		backdrop.dim().mod().height(this.height - 65).flush();

		context.addChildren(backdrop);

		context.addChildren(this.layerTab, this.postGenTab);

		this.layerViewer = new GuiListViewer<>
				(
						Dim2D.build().width(156).height(120).x(32).y(56).flush(),
						navigator -> navigator.getNodes().inverse().values().stream().max(Comparator.naturalOrder())
								.orElse(-1) + 1,
						this.container.getNavigator(),
						(p, n, i) -> new ReplaceLayerSlot(Dim2D.build().pos(p).flush(), i),
						i ->
						{
							SlotHashed slot1 = new SlotHashed(this.container.stackerInventory, (i * 2) + 37, 6, 6 + ((i * 24) % 120));
							SlotHashed slot2 = new SlotHashed(this.container.stackerInventory, (i * 2) + 37 + 1, 37, 6 + ((i * 24) % 120));

							return new SlotGroup(Pair.of(slot1, slot2), i);
						},
						24
				);

		this.layerViewer.listen(this);
		this.container.getNavigator().addListener(this);

		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).x(center.x() + 63).y(center.y() - 3.5F).center(true).flush(),
				BLUEPRINT_INVENTORY);

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		if (true)
		{
			List<Runnable> calls = Lists.newArrayList();

			for (Map.Entry<Integer, PostGenReplaceLayer> e : this.blueprint.getData().getPostGenReplaceLayers().entrySet())
			{
				int i = e.getKey();
				PostGenReplaceLayer layer = e.getValue();

				// To prevent concurrent modification exception
				calls.add(() ->
				{
					this.container.stackerInventory.expand((i * 2) + 37 + 2);

					SlotHashed slot1 = new SlotHashed(this.container.stackerInventory, (i * 2) + 37, 6, 6 + ((i * 24) % 120));
					SlotHashed slot2 = new SlotHashed(this.container.stackerInventory, (i * 2) + 37 + 1, 37, 6 + ((i * 24) % 120));

					Pair<SlotHashed, SlotHashed> pair = Pair.of(slot1, slot2);

					this.layerViewer.getNavigator().put(new SlotGroup(pair, i), i, false);

					this.container.stackerInventory.setInventorySlotContents((i * 2) + 37, layer.getRequired());
					this.container.stackerInventory.setInventorySlotContents((i * 2) + 37 + 1, layer.getReplaced());
				});
			}

			calls.forEach(Runnable::run);
			calls.clear();
		}

		context.addChildren(this.layerViewer, inventory);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.closeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPreviousViewer().getActualScreen());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (this.saveButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.blueprint, BlueprintData.EXTENSION));
		}
	}

	private void displaySlotsToServer()
	{
		int[] slots = new int[this.layerViewer.getVisibleNodes().size()];

		for (int i = 0; i < this.layerViewer.getVisibleNodes().size(); i++)
		{
			SlotGroup pair = this.layerViewer.getVisibleNodes().get(i);
			int id = this.layerViewer.findPosition(pair);

			slots[i] = id;
		}

		OrbisCore.network().sendPacketToServer(new PacketPostGenDisplayLayers(slots));
	}

	@Override
	public void onScroll(int currentScroll, int newScroll)
	{
		this.container.display(this.layerViewer.getVisibleNodes());

		this.displaySlotsToServer();
	}

	@Override
	public void onRemoveNode(SlotGroup node, int index)
	{
		OrbisCore.network()
				.sendPacketToServer(new PacketPostGenRemoveLayer(index));

		this.container.removeLayer(node);

		this.container.stackerInventory.remove((index * 2) + 37);
		this.container.stackerInventory.remove((index * 2) + 37 + 1);

		this.container.display(this.layerViewer.getVisibleNodes());

		this.displaySlotsToServer();

		if (this.blueprint.getData().getMetadata().getIdentifier() == null)
		{
			OrbisCore.network().sendPacketToServer(
					new PacketBlueprintRemovePostGenReplaceLayer(this.blueprint, index));
		}
		else
		{
			OrbisCore.network().sendPacketToServer(
					new PacketBlueprintRemovePostGenReplaceLayer(this.blueprint.getData().getMetadata().getIdentifier(), index));
		}
	}

	@Override
	public void onAddNode(SlotGroup node, int index, boolean newNode)
	{
		this.container.stackerInventory.expand((index * 2) + 37 + 2);

		this.container.addLayer(node);

		OrbisCore.network().sendPacketToServer(
				new PacketPostGenAddLayer(index, node.getSlots().getLeft().xPos, node.getSlots().getLeft().yPos, node.getSlots().getRight().xPos,
						node.getSlots().getRight().yPos));

		this.container.display(this.layerViewer.getVisibleNodes());

		this.displaySlotsToServer();

		if (!newNode)
		{
			return;
		}

		final Blueprint b = this.blueprint;

		if (Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			b.getData().setPostGenReplaceLayer(index, new PostGenReplaceLayer(ItemStack.EMPTY, ItemStack.EMPTY));
		}
		else
		{
			if (b.getData().getMetadata().getIdentifier() == null)
			{
				OrbisCore.network().sendPacketToServer(
						new PacketBlueprintAddPostGenReplaceLayer(b, index));
			}
			else
			{
				OrbisCore.network().sendPacketToServer(
						new PacketBlueprintAddPostGenReplaceLayer(b.getData().getMetadata().getIdentifier(), index));
			}
		}
	}

	@Override
	public void onNodeClicked(SlotGroup node, int index)
	{

	}

	public static class ReplaceLayerSlot extends GuiElement
	{
		private static ResourceLocation TEXTURE = OrbisCore.getResource("blueprint_gui/replace_layer_slot.png");

		private GuiTexture bg;

		private GuiText text;

		private int layerId;

		public ReplaceLayerSlot(Rect rect, int layerId)
		{
			super(rect, false);

			this.layerId = layerId;

			this.dim().mod().width(136).height(24).flush();
		}

		@Override
		public void build()
		{
			this.bg = new GuiTexture(Dim2D.build().width(136).height(24).flush(), TEXTURE);
			this.text = new GuiText(Dim2D.build().x(60).y(6).flush(), new Text(new TextComponentString(String.valueOf(this.layerId)), 1.0F));

			this.context().addChildren(this.bg, this.text);
		}
	}

}
