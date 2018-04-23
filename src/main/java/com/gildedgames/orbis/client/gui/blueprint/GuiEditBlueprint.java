package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.client.gui.util.list.GuiListViewer;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanillaToggled;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.data.schedules.ScheduleLayer;
import com.gildedgames.orbis_api.util.Callback;
import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis_api.client.gui.data.list.ListNavigator;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.schedules.GuiScheduleLayerPanel;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintAddScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintRemoveScheduleLayer;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketBlueprintSetCurrentScheduleLayer;
import com.gildedgames.orbis_api.util.InputHelper;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GuiEditBlueprint extends GuiFrame
{
	private static final ResourceLocation VIEWER_BACKDROP = OrbisCore.getResource("list/list_viewer.png");

	private final Blueprint blueprint;

	private GuiButtonVanilla saveButton, closeButton;

	private GuiListViewer<IScheduleLayer, GuiButtonVanillaToggled> layerViewer;

	private GuiScheduleLayerPanel currentPanel;

	private Map<Integer, GuiScheduleLayerPanel> cachedPanels = Maps.newHashMap();

	public GuiEditBlueprint(GuiFrame prevFrame, final Blueprint blueprint)
	{
		super(prevFrame, Dim2D.flush());

		this.setDrawDefaultBackground(true);
		this.blueprint = blueprint;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		GuiButtonVanillaToggled.TOGGLED_BUTTON_ID = -1;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(75).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.closeButton.getInner().displayString = "Close";

		this.addChildren(this.saveButton);
		this.addChildren(this.closeButton);

		this.layerViewer = new GuiListViewer<IScheduleLayer, GuiButtonVanillaToggled>(
				Dim2D.build().width(156).pos(Pos2D.build().add(30, 55).flush()).flush(),
				navigator -> this.blueprint.getData().findNextAvailableId(), new ListNavigator<>(), (p, n, i) ->
		{
			final GuiButtonVanillaToggled button = new GuiButtonVanillaToggled(Dim2D.build().pos(p).width(130).height(20).flush(), i);

			button.getInner().displayString = n.getDisplayName();

			if (n == GuiEditBlueprint.this.blueprint.getCurrentScheduleLayer())
			{
				GuiButtonVanillaToggled.TOGGLED_BUTTON_ID = i;
			}

			return button;
		}, i -> new ScheduleLayer("Layer " + String.valueOf(i + 1),
				GuiEditBlueprint.this.blueprint), 20)
		{
			@Override
			public void onAddNode(final IScheduleLayer node, final int index, boolean newNode)
			{
				super.onAddNode(node, index, newNode);

				if (!newNode)
				{
					return;
				}

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				if (Minecraft.getMinecraft().isIntegratedServerRunning())
				{
					b.getData().addScheduleLayer(node);
				}
				else
				{
					if (b.getData().getMetadata().getIdentifier() == null)
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b, node.getDisplayName()));
					}
					else
					{
						OrbisCore.network().sendPacketToServer(
								new PacketBlueprintAddScheduleLayer(b.getData().getMetadata().getIdentifier(), node.getDisplayName()));
					}
				}

				GuiScheduleLayerPanel panel = new GuiScheduleLayerPanel(Dim2D.build().width(this.width - 250).height(this.height - 65).flush(),
						GuiEditBlueprint.this.blueprint, node);

				panel.dim().mod().addX(this.width - panel.dim().width() - 20).addY(45).flush();

				GuiEditBlueprint.this.cachedPanels.put(index, panel);
			}

			@Override
			public void onRemoveNode(final IScheduleLayer node, final int index)
			{
				super.onRemoveNode(node, index);

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				if (b.getData().getMetadata().getIdentifier() == null)
				{
					OrbisCore.network().sendPacketToServer(
							new PacketBlueprintRemoveScheduleLayer(b, b.getData().getScheduleLayerId(node)));
				}
				else
				{
					OrbisCore.network().sendPacketToServer(
							new PacketBlueprintRemoveScheduleLayer(b.getData().getMetadata().getIdentifier(), b.getData().getScheduleLayerId(node)));
				}
			}

			@Override
			public void onNodeClicked(final IScheduleLayer node, final int index)
			{
				super.onNodeClicked(node, index);

				final Blueprint b = GuiEditBlueprint.this.blueprint;

				final int layerIndex = b.getData().getScheduleLayerId(node);

				GuiEditBlueprint.this.removeChild(GuiEditBlueprint.this.currentPanel);

				if (layerIndex != -1)
				{
					OrbisCore.network().sendPacketToServer(new PacketBlueprintSetCurrentScheduleLayer(b, layerIndex));

					GuiEditBlueprint.this.currentPanel = GuiEditBlueprint.this.cachedPanels.get(index);

					if (GuiEditBlueprint.this.currentPanel != null)
					{
						GuiEditBlueprint.this.addChildren(GuiEditBlueprint.this.currentPanel);
					}
				}
				else
				{
					OrbisCore.LOGGER.error("Layer index is -1 while trying to click on a node in GuiSaveData.");
				}
			}
		};

		final GuiTexture backdrop = new GuiTexture(Dim2D.build().pos(Pos2D.build().add(20, 45).flush()).width(176).flush(), VIEWER_BACKDROP);

		this.layerViewer.dim().mod().height(this.height - 85).flush();
		backdrop.dim().mod().height(this.height - 65).flush();

		this.addChildren(backdrop);

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		List<Callback> calls = Lists.newArrayList();

		for (Map.Entry<Integer, IScheduleLayer> e : this.blueprint.getData().getScheduleLayers().entrySet())
		{
			int i = e.getKey();
			IScheduleLayer layer = e.getValue();

			// To prevent concurrent modification exception
			calls.add(() ->
			{
				this.layerViewer.getNavigator().put(layer, i, false);

				GuiScheduleLayerPanel panel = new GuiScheduleLayerPanel(Dim2D.build().width(this.width - 250).height(this.height - 65).flush(), this.blueprint,
						layer);

				panel.dim().mod().addX(this.width - panel.dim().width() - 20).addY(45).flush();

				GuiEditBlueprint.this.cachedPanels.put(i, panel);
			});
		}

		calls.forEach(Callback::call);
		calls.clear();

		this.addChildren(this.layerViewer);

		int currentIndex = this.blueprint.getCurrentScheduleLayerIndex();

		if (!this.cachedPanels.isEmpty())
		{
			GuiEditBlueprint.this.currentPanel = GuiEditBlueprint.this.cachedPanels.get(currentIndex);

			if (GuiEditBlueprint.this.currentPanel != null)
			{
				GuiEditBlueprint.this.addChildren(GuiEditBlueprint.this.currentPanel);
			}
		}
	}

	@Override
	public void draw()
	{
		super.draw();
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.blueprint, BlueprintData.EXTENSION));
		}
	}
}
