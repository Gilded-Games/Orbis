package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_types.ISelectionType;

public class GuiChoiceMenuSelectionTypes extends GuiChoiceMenu
{

	public GuiChoiceMenuSelectionTypes(final PlayerOrbis playerOrbis)
	{
		super();

		this.choices = new Choice[playerOrbis.selectionTypes().array().length];

		for (int i = 0; i < playerOrbis.selectionTypes().array().length; i++)
		{
			final ISelectionType selectionType = playerOrbis.selectionTypes().array()[i];
			final Choice choice = new SelectionTypeChoice(selectionType, selectionType.getClient().displayName());

			this.choices[i] = choice;
		}
	}

	public class SelectionTypeChoice implements Choice
	{
		private final String name;

		private final ISelectionType selectionType;

		public SelectionTypeChoice(final ISelectionType power, final String name)
		{
			this.selectionType = power;
			this.name = name;
		}

		@Override
		public void onSelect(final PlayerOrbis playerOrbis)
		{
			playerOrbis.selectionTypes().setCurrentSelectionType(this.selectionType.getClass());
		}

		@Override
		public GuiTexture getIcon()
		{
			return this.selectionType.getClient().getIcon();
		}

		@Override
		public String name()
		{
			return this.name;
		}
	}

}
