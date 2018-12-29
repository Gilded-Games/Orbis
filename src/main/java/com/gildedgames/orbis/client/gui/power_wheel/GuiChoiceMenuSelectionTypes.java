package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.player.designer_mode.ISelectionType;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;

public class GuiChoiceMenuSelectionTypes extends GuiChoiceMenu
{

	public GuiChoiceMenuSelectionTypes(final PlayerOrbis playerOrbis)
	{
		super();

		this.choices = new Choice[playerOrbis.selectionTypes().getSelectionTypes().size()];

		int i = 0;

		for (ISelectionType selectionType : playerOrbis.selectionTypes().getSelectionTypes())
		{
			final Choice choice = new SelectionTypeChoice(selectionType, selectionType.getClient().displayName());

			this.choices[i] = choice;

			i++;
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
			playerOrbis.selectionTypes().setCurrentSelectionType(this.selectionType);
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
