package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.data.directory.IDirectoryNavigator;
import com.gildedgames.orbis_api.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.io.File;
import java.util.Collections;

public class GuiFactory
{

	public static final ResourceLocation REFRESH_ICON = OrbisCore.getResource("navigator/refresh.png");

	public static final ResourceLocation REFRESH_ICON_CLICKED = OrbisCore.getResource("navigator/refresh_clicked.png");

	public static final ResourceLocation REFRESH_ICON_DISABLED = OrbisCore.getResource("navigator/refresh_disabled.png");

	public static final ResourceLocation REFRESH_ICON_HOVERED = OrbisCore.getResource("navigator/refresh_hovered.png");

	public static final ResourceLocation LEFT_ARROW_ICON = OrbisCore.getResource("navigator/left_arrow.png");

	public static final ResourceLocation LEFT_ARROW_ICON_CLICKED = OrbisCore.getResource("navigator/left_arrow_clicked.png");

	public static final ResourceLocation LEFT_ARROW_ICON_DISABLED = OrbisCore.getResource("navigator/left_arrow_disabled.png");

	public static final ResourceLocation LEFT_ARROW_ICON_HOVERED = OrbisCore.getResource("navigator/left_arrow_hovered.png");

	public static final ResourceLocation RIGHT_ARROW_ICON = OrbisCore.getResource("navigator/right_arrow.png");

	public static final ResourceLocation RIGHT_ARROW_ICON_CLICKED = OrbisCore.getResource("navigator/right_arrow_clicked.png");

	public static final ResourceLocation RIGHT_ARROW_ICON_DISABLED = OrbisCore.getResource("navigator/right_arrow_disabled.png");

	public static final ResourceLocation RIGHT_ARROW_ICON_HOVERED = OrbisCore.getResource("navigator/right_arrow_hovered.png");

	public static final ResourceLocation FORGE_BUTTON = OrbisCore.getResource("filter_gui/forge_button.png");

	public static final ResourceLocation FORGE_BUTTON_CLICKED = OrbisCore.getResource("filter_gui/forge_button_clicked.png");

	public static final ResourceLocation FORGE_BUTTON_DISABLED = OrbisCore.getResource("filter_gui/forge_button_disabled.png");

	public static final ResourceLocation FORGE_BUTTON_HOVERED = OrbisCore.getResource("filter_gui/forge_button_hovered.png");

	public static final ResourceLocation DELETE = OrbisCore.getResource("list/delete.png");

	public static final ResourceLocation DELETE_CLICKED = OrbisCore.getResource("list/delete_clicked.png");

	public static final ResourceLocation DELETE_DISABLED = OrbisCore.getResource("list/delete_disabled.png");

	public static final ResourceLocation DELETE_HOVERED = OrbisCore.getResource("list/delete_hovered.png");

	public static final ResourceLocation ADD = OrbisCore.getResource("list/add.png");

	public static final ResourceLocation ADD_CLICKED = OrbisCore.getResource("list/add_clicked.png");

	public static final ResourceLocation ADD_DISABLED = OrbisCore.getResource("list/add_disabled.png");

	public static final ResourceLocation ADD_HOVERED = OrbisCore.getResource("list/add_hovered.png");

	private GuiFactory()
	{

	}

	public static IDropdownElement createCloseDropdownElement(final File file, final IDirectoryNavigator navigator)
	{
		return new DropdownElement(new TextComponentString("Close"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				list.setDropdownElements(Collections.emptyList());
			}
		};
	}

	public static IDropdownElement createDeleteFileDropdownElement(final File file, final IDirectoryNavigator navigator)
	{
		return new DropdownElement(new TextComponentString("Delete"))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				if (file.isDirectory())
				{
					//FileHelper.deleteDirectory(file);
				}
				else
				{
					//file.delete();
				}

				list.setDropdownElements(Collections.emptyList());
				list.setVisible(false);

				navigator.refresh();
			}
		};
	}

	public static GuiAbstractButton createForgeButton()
	{
		final Rect rect = Dim2D.build().width(22).height(22).flush();

		final GuiTexture defaultState = new GuiTexture(rect, FORGE_BUTTON);
		final GuiTexture hoveredState = new GuiTexture(rect, FORGE_BUTTON_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, FORGE_BUTTON_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, FORGE_BUTTON_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

	public static GuiAbstractButton createDeleteButton()
	{
		final Rect rect = Dim2D.build().width(20).height(20).flush();

		final GuiTexture defaultState = new GuiTexture(rect, DELETE);
		final GuiTexture hoveredState = new GuiTexture(rect, DELETE_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, DELETE_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, DELETE_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

	public static GuiAbstractButton createAddButton()
	{
		final Rect rect = Dim2D.build().width(20).height(20).flush();

		final GuiTexture defaultState = new GuiTexture(rect, ADD);
		final GuiTexture hoveredState = new GuiTexture(rect, ADD_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, ADD_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, ADD_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

	public static GuiAbstractButton createRefreshButton()
	{
		final Rect rect = Dim2D.build().width(10).height(10).flush();

		final GuiTexture defaultState = new GuiTexture(rect, REFRESH_ICON);
		final GuiTexture hoveredState = new GuiTexture(rect, REFRESH_ICON_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, REFRESH_ICON_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, REFRESH_ICON_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

	public static GuiAbstractButton createLeftArrowButton()
	{
		final Rect rect = Dim2D.build().width(15).height(12).flush();

		final GuiTexture defaultState = new GuiTexture(rect, LEFT_ARROW_ICON);
		final GuiTexture hoveredState = new GuiTexture(rect, LEFT_ARROW_ICON_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, LEFT_ARROW_ICON_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, LEFT_ARROW_ICON_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

	public static GuiAbstractButton createRightArrowButton()
	{
		final Rect rect = Dim2D.build().width(15).height(12).flush();

		final GuiTexture defaultState = new GuiTexture(rect, RIGHT_ARROW_ICON);
		final GuiTexture hoveredState = new GuiTexture(rect, RIGHT_ARROW_ICON_HOVERED);
		final GuiTexture clickedState = new GuiTexture(rect, RIGHT_ARROW_ICON_CLICKED);
		final GuiTexture disabledState = new GuiTexture(rect, RIGHT_ARROW_ICON_DISABLED);

		final GuiAbstractButton button = new GuiAbstractButton(rect, defaultState, hoveredState, clickedState, disabledState);

		return button;
	}

}
