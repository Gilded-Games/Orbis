package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketChangePower;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GuiChoiceMenuPowers extends GuiChoiceMenu
{

	public GuiChoiceMenuPowers(final PlayerOrbis playerOrbis)
	{
		super();

		boolean experimental = OrbisCore.CONFIG.useExperimentalFeatures();

		Predicate<IGodPower> filter = (p) -> experimental || (p != playerOrbis.powers().getBlueprintPower() && p != playerOrbis.powers().getEntrancePower());
		List<IGodPower> filteredPowers = Arrays.stream(playerOrbis.powers().array()).filter(filter).collect(Collectors.toList());
		this.choices = new Choice[filteredPowers.size()];

		for (int i = 0; i < filteredPowers.size(); i++)
		{
			final IGodPower power = filteredPowers.get(i);
			final GuiChoiceMenu.Choice choice = new PowerChoice(power, power.getClientHandler().displayName());

			this.choices[i] = choice;
		}
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

			OrbisCore.network().sendPacketToServer(new PacketChangePower(playerOrbis.powers().getCurrentPowerIndex()));
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
