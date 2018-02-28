package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;

public class GuiChoiceMenuSelectionInputs extends GuiChoiceMenu
{

	public GuiChoiceMenuSelectionInputs(final PlayerOrbis playerOrbis)
	{
		super();

		this.choices = new Choice[playerOrbis.selectionInputs().array().length];

		for (int i = 0; i < playerOrbis.selectionInputs().array().length; i++)
		{
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().array()[i];
			final Choice choice = new SelectionInputChoice(selectionInput, selectionInput.getClient().displayName());

			this.choices[i] = choice;
		}
	}

	public class SelectionInputChoice implements Choice
	{
		private final String name;

		private final ISelectionInput selectionInput;

		public SelectionInputChoice(final ISelectionInput power, final String name)
		{
			this.selectionInput = power;
			this.name = name;
		}

		@Override
		public void onSelect(final PlayerOrbis playerOrbis)
		{
			playerOrbis.selectionInputs().setCurrentSelectionInput(playerOrbis.selectionInputs().getSelectionInputIndex(this.selectionInput));
		}

		@Override
		public GuiTexture getIcon()
		{
			return this.selectionInput.getClient().getIcon();
		}

		@Override
		public String name()
		{
			return this.name;
		}
	}

}
