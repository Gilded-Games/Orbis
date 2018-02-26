package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.PacketChangePower;
import com.gildedgames.orbis.common.player.godmode.IGodPower;

public class GuiChoiceMenuPowers extends GuiChoiceMenu
{

	public GuiChoiceMenuPowers(final PlayerOrbis playerOrbis)
	{
		super();

		this.choices = new Choice[playerOrbis.powers().array().length];

		for (int i = 0; i < playerOrbis.powers().array().length; i++)
		{
			final IGodPower power = playerOrbis.powers().array()[i];
			final GuiChoiceMenu.Choice choice = new PowerChoice(power, power.getClientHandler().displayName());

			this.choices[i] = choice;
		}
	}

	@Override
	public void init()
	{
		super.init();
	}

	public class PowerChoice implements Choice
	{
		private final String name;

		private final IGodPower power;

		public PowerChoice(final IGodPower power, final String name)
		{
			this.power = power;
			this.name = name;
		}

		@Override
		public void onSelect(final PlayerOrbis playerOrbis)
		{
			playerOrbis.powers().setCurrentPower(this.power.getClass());

			NetworkingOrbis.sendPacketToServer(new PacketChangePower(playerOrbis.powers().getCurrentPowerIndex()));
		}

		@Override
		public GuiTexture getIcon()
		{
			return this.power.getClientHandler().getIcon();
		}

		@Override
		public String name()
		{
			return this.name;
		}
	}

}
