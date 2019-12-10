package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.gui.util.GuiFactoryOrbis;
import com.gildedgames.orbis.client.gui.util.directory.GuiDirectoryViewer;
import com.gildedgames.orbis.client.gui.util.directory.nodes.*;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.ContainerLoadData;
import com.gildedgames.orbis.common.items.*;
import com.gildedgames.orbis.common.network.packets.PacketSetItemStack;
import com.gildedgames.orbis.common.network.packets.projects.PacketRequestProjectListing;
import com.gildedgames.orbis.lib.OrbisLib;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.data.directory.DirectoryNavigator;
import com.gildedgames.orbis.lib.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis.lib.client.gui.data.directory.IDirectoryNavigatorListener;
import com.gildedgames.orbis.lib.client.gui.data.directory.INavigatorNode;
import com.gildedgames.orbis.lib.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis.lib.client.gui.util.GuiText;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.data.DataCondition;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.management.IProject;
import com.gildedgames.orbis.lib.util.mc.InventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GuiLoadData extends GuiViewer implements IDirectoryNavigatorListener
{
	private static final ResourceLocation MATRIX_ICON = OrbisCore.getResource("filter_gui/filter_matrix.png");

	private static final ResourceLocation BLUEPRINT_INVENTORY = OrbisCore.getResource("blueprint_gui/blueprint_inventory.png");

	private static final ResourceLocation MERGE_ICON = OrbisCore.getResource("blueprint_gui/merge_icon_right.png");

	private static final ResourceLocation TAB = OrbisCore.getResource("blueprint_gui/tab.png");

	private static final ResourceLocation TAB_PRESSED = OrbisCore.getResource("blueprint_gui/tab_pressed.png");

	private static final ResourceLocation SEARCH = OrbisCore.getResource("blueprint_gui/search.png");

	private static final ResourceLocation STACKER = OrbisCore.getResource("blueprint_gui/stacker.png");

	private final int tabCount = 2;

	private final ContainerLoadData container;

	private int tabIndex;

	private GuiTexture[] tabs = new GuiTexture[this.tabCount];

	private GuiElement[] tabFrames = new GuiElement[this.tabCount];

	private GuiAbstractButton forgeButton;

	private GuiTexture matrix, flow;

	private GuiText combineTitle;

	private GuiDirectoryViewer directoryViewer;

	private boolean requestListing = true;

	private IProject project;

	public GuiLoadData(GuiViewer prevFrame, final PlayerOrbis playerOrbis)
	{
		super(new GuiElement(Dim2D.flush(), false), prevFrame, null);

		this.container = new ContainerLoadData(playerOrbis, playerOrbis.powers().getBlueprintPower().getForgeInventory());

		this.inventorySlots = this.container;
		playerOrbis.getEntity().openContainer = this.inventorySlots;

		this.allowUserInput = true;
	}

	public void refreshNavigator()
	{
		this.requestListing = false;
		this.directoryViewer.getNavigator().refresh();
		this.requestListing = true;
	}

	@Override
	public void initContainerSize()
	{
		this.guiLeft = this.width / 2 - 122 - (176 / 2);
		this.guiTop = this.height / 2 - (147 / 2) - 12;

		this.xSize = 179 * 2;
		this.ySize = 169;
	}

	@Override
	public void build(IGuiContext context)
	{
		this.getViewing().dim().mod().width(179 * 2).height(169).x(this.width / 2 - 90 - (176 / 2)).y(this.height / 2 - (147 / 2) + 12).flush();

		this.directoryViewer = new GuiDirectoryViewer(Pos2D.build().addX(80).addY(61).flush(),
				new DirectoryNavigator(new OrbisNavigatorNodeFactory()));

		this.directoryViewer.dim().mod().center(true).flush();

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}

		this.directoryViewer.getNavigator().addListener(this);

		this.directoryViewer.getNavigator().openDirectory(OrbisCore.getProjectManager().getLocation());

		this.directoryViewer.getNavigator()
				.injectDirectories(OrbisCore.getProjectManager().getLocation(), OrbisCore.getProjectManager().getExtraProjectSourceFolders());

		this.tabFrames[0] = this.directoryViewer;
		this.tabFrames[1] = new GuiElement(Dim2D.flush(), false);

		//TODO:
		/*GuiCreateBlueprintStacker bp = new GuiCreateBlueprintStacker(this, this.container);

		bp.dim().mod().addX(-8).addY(-34).flush();

		this.tabFrames[1] = bp;*/

		final int xOffset = 15;
		final int yOffset = 7;

		this.forgeButton = GuiFactoryOrbis.createForgeButton();

		this.forgeButton.dim().mod().center(true).addY(yOffset + 7).addX(315 + xOffset).flush();

		this.matrix = new GuiTexture(Dim2D.build().width(85).height(105).addX(180 + xOffset).addY(-54 + yOffset).flush(), MATRIX_ICON);
		GuiTexture inventory = new GuiTexture(Dim2D.build().width(176).height(90).x(180).y(66).flush(),
				BLUEPRINT_INVENTORY);
		this.flow = new GuiTexture(Dim2D.build().width(20).height(14).addX(275 + xOffset).addY(yOffset).flush(), MERGE_ICON);

		this.combineTitle = new GuiText(Dim2D.build().centerX(true).addX(223 + xOffset).addY(yOffset - 44).flush(),
				new Text(new TextComponentString("Group"), 1.0F));

		context.addChildren(this.matrix, this.flow, this.combineTitle, this.forgeButton, inventory);

		this.forgeButton.state().setCanBeTopHoverElement(true);

		for (int i = 0; i < this.tabCount; i++)
		{
			GuiTexture tab = new GuiTexture(Dim2D.build().width(22).height(19).flush(), TAB);

			tab.dim().mod().addY(-53).addX(i * 23).flush();

			this.tabs[i] = tab;
			this.tabFrames[i].state().setVisible(false);

			context.addChildren(tab);
			context.addChildren(this.tabFrames[i]);

			tab.state().setCanBeTopHoverElement(true);
		}

		this.setTabIndex(0);

		GuiTexture search = new GuiTexture(Dim2D.build().pos(Pos2D.build().x(5).y(-48).flush()).width(13).height(13).flush(), SEARCH);
		GuiTexture stacker = new GuiTexture(Dim2D.build().pos(Pos2D.build().x(27).y(-48).flush()).width(13).height(13).flush(), STACKER);

		context.addChildren(search, stacker);
	}

	public void setTabIndex(int index)
	{
		this.tabIndex = index;

		for (int i = 0; i < this.tabCount; i++)
		{
			GuiTexture tab = this.tabs[i];

			if (i == this.tabIndex)
			{
				tab.setResourceLocation(TAB_PRESSED);
				this.tabFrames[i].state().setVisible(true);
				this.tabFrames[i].state().setEnabled(true);
			}
			else
			{
				tab.setResourceLocation(TAB);
				this.tabFrames[i].state().setVisible(false);
				this.tabFrames[i].state().setEnabled(false);
			}
		}

		if (index == 1)
		{
			this.container.startStackerInterface();
		}
		else
		{
			this.container.stopStackerInterface();
		}
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
	}

	@Override
	protected void handleMouseClick(@Nullable Slot slotIn, int slotId, int mouseButton, ClickType type)
	{
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks)
	{
		//Gui.drawRect((int) this.dim().x(), (int) this.dim().y(), (int) this.dim().maxX(), (int) this.dim().maxY(), Integer.MIN_VALUE);

		this.forgeButton.state().setEnabled(InventoryHelper.getItemStacks(this.container.slots).size() >= 2);

		super.drawScreen(mouseX, mouseY, partialTicks);

		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
	{
	}

	@Override
	public void drawDefaultBackground()
	{
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		for (int i = 0; i < this.tabs.length; i++)
		{
			GuiTexture tab = this.tabs[i];

			if (tab.state().isHoveredAndTopElement())
			{
				this.setTabIndex(i);
			}
		}

		if (this.forgeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.blueprint_palette);

			final BlueprintDataPalette palette = new BlueprintDataPalette();

			for (final ItemStack s : InventoryHelper.getItemStacks(this.container.slots))
			{
				final IDataIdentifier id = ItemBlueprint.getBlueprintId(s);

				if (palette.getIDToConditions().containsKey(id))
				{
					final DataCondition condition = palette.getIDToConditions().get(id);

					condition.setWeight(condition.getWeight() + 1.0F);
				}
				else
				{
					final Optional<BlueprintData> data = OrbisCore.getProjectManager().findData(id);

					if (data.isPresent())
					{
						final DataCondition condition = new DataCondition();

						condition.setWeight(s.getCount());

						palette.add(data.get(), condition);
					}
					else
					{
						OrbisLib.LOGGER.error("Could not find data in GuiLoadData", id);
					}
				}
			}

			ItemBlueprintPalette.setBlueprintPalette(stack, palette);

			OrbisCore.network().sendPacketToServer(new PacketSetItemStack(stack));
			Minecraft.getMinecraft().player.inventory.setItemStack(stack);
		}
	}

	@Override
	public void onNodeClick(IDirectoryNavigator navigator, INavigatorNode node)
	{
		if (node instanceof NavigatorNodeBlueprint)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.blueprint);

			final Optional<IData> data = OrbisCore.getProjectManager().findData(this.project, node.getFile());

			if (data.isPresent())
			{
				ItemStack onMouse = this.mc.player.inventory.getItemStack();

				if (onMouse.getItem() instanceof ItemBlueprint && data.get().getMetadata().getIdentifier().equals(ItemBlueprint.getBlueprintId(onMouse)))
				{
					onMouse.setCount(Math.min(64, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 64 : onMouse.getCount() + 1));

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(onMouse));
				}
				else
				{
					ItemBlueprint.setBlueprint(stack, data.get().getMetadata().getIdentifier());

					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
					{
						stack.setCount(64);
					}

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(stack));
					Minecraft.getMinecraft().player.inventory.setItemStack(stack);
				}
			}
			else
			{
				OrbisCore.LOGGER.info("Could not load data: " + node.getFile() + " - Project: " + this.project, node.getFile());
			}
		}
		else if (node instanceof NavigatorNodeFramework)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.framework);

			final Optional<IData> data = OrbisCore.getProjectManager().findData(GuiLoadData.this.project, node.getFile());

			if (data.isPresent())
			{
				ItemStack onMouse = this.mc.player.inventory.getItemStack();

				if (onMouse.getItem() instanceof ItemFramework && data.get().getMetadata().getIdentifier().equals(ItemFramework.getDataId(onMouse)))
				{
					onMouse.setCount(Math.min(64, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 64 : onMouse.getCount() + 1));

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(onMouse));
				}
				else
				{
					ItemFramework.setDataId(stack, data.get().getMetadata().getIdentifier());

					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
					{
						stack.setCount(64);
					}

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(stack));
					Minecraft.getMinecraft().player.inventory.setItemStack(stack);
				}
			}
			else
			{
				OrbisCore.LOGGER.info("Could not load data: " + node.getFile() + " - Project: " + this.project, node.getFile());
			}
		}
		else if (node instanceof NavigatorNodeBlueprintStacker)
		{
			final ItemStack stack = new ItemStack(ItemsOrbis.blueprint_stacker);

			Optional<IData> data = OrbisCore.getProjectManager().findData(GuiLoadData.this.project, node.getFile());

			if (data.isPresent())
			{
				ItemStack onMouse = this.mc.player.inventory.getItemStack();

				if (onMouse.getItem() instanceof ItemBlueprintStacker && data.get().getMetadata().getIdentifier()
						.equals(ItemBlueprintStacker.getBlueprintStackerId(onMouse)))
				{
					onMouse.setCount(Math.min(64, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 64 : onMouse.getCount() + 1));

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(onMouse));
				}
				else
				{
					ItemBlueprintStacker.setBlueprintStacker(stack, (BlueprintStackerData) data.get());

					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
					{
						stack.setCount(64);
					}

					OrbisCore.network().sendPacketToServer(new PacketSetItemStack(stack));
					Minecraft.getMinecraft().player.inventory.setItemStack(stack);
				}
			}
			else
			{
				OrbisCore.LOGGER.info("Could not load data: " + node.getFile() + " - Project: " + this.project, node.getFile());
			}
		}
	}

	@Override
	public void onNodeOpen(final IDirectoryNavigator navigator, final INavigatorNode node)
	{
		if (node instanceof NavigatorNodeProject)
		{
			final NavigatorNodeProject projectNode = (NavigatorNodeProject) node;

			this.project = projectNode.getProject();
		}
	}

	@Override
	public void onDirectoryOpen(final IDirectoryNavigator navigator, final File file)
	{

	}

	@Override
	public void onBack(final IDirectoryNavigator navigator)
	{

	}

	@Override
	public void onForward(final IDirectoryNavigator navigator)
	{

	}

	@Override
	public void onRefresh(final IDirectoryNavigator navigator)
	{
		if (!Minecraft.getMinecraft().isIntegratedServerRunning() && this.requestListing)
		{
			OrbisCore.network().sendPacketToServer(new PacketRequestProjectListing());
		}
	}

}
