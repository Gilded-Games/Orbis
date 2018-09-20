package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.PacketChangePower;
import com.gildedgames.orbis.common.player.godmode.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;

public class PlayerPowerModule extends PlayerOrbisModule
{
	private final IGodPower[] powers;

	private final GodPowerCreative creativePower;

	private final GodPowerBlueprint blueprintPower;

	private final GodPowerFill fillPower;

	private final GodPowerDelete deletePower;

	private final GodPowerReplace replacePower;

	private final GodPowerSelect selectPower;

	private final GodPowerSpectator spectatorPower;

	private final GodPowerEntrance entrancePower;

	private final GodPowerPathway pathwayPower;

	private final GodPowerFramework frameworkPower;

	private int currentPowerIndex;

	private boolean scheduling;

	public PlayerPowerModule(final PlayerOrbis playerOrbis)
	{
		super(playerOrbis);

		this.creativePower = new GodPowerCreative(this.getWorld());
		this.fillPower = new GodPowerFill(playerOrbis, this.getWorld());
		this.deletePower = new GodPowerDelete(this.getWorld());
		this.replacePower = new GodPowerReplace(this.getWorld());
		this.blueprintPower = new GodPowerBlueprint(playerOrbis, this.getWorld());
		this.selectPower = new GodPowerSelect(this.getWorld());
		this.spectatorPower = new GodPowerSpectator(this.getWorld());
		this.entrancePower = new GodPowerEntrance(this.getWorld());
		this.pathwayPower = new GodPowerPathway(this.getWorld());
		this.frameworkPower = new GodPowerFramework(this.getWorld());

		final Collection<IGodPower> powers = new ArrayList<>();

		powers.add(this.creativePower);
		powers.add(this.fillPower);
		powers.add(this.deletePower);
		powers.add(this.replacePower);
		powers.add(this.selectPower);
		powers.add(this.spectatorPower);
		powers.add(this.blueprintPower);

		this.powers = powers.toArray(new IGodPower[powers.size()]);
	}

	public boolean isScheduling()
	{
		return this.scheduling;
	}

	public void setScheduling(boolean scheduling)
	{
		this.scheduling = scheduling;
	}

	public GodPowerFramework getFrameworkPower()
	{
		return this.frameworkPower;
	}

	public GodPowerPathway getPathwayPower()
	{
		return this.pathwayPower;
	}

	public GodPowerEntrance getEntrancePower()
	{
		return this.entrancePower;
	}

	public GodPowerSpectator getSpectatorPower()
	{
		return this.spectatorPower;
	}

	public GodPowerReplace getReplacePower()
	{
		return this.replacePower;
	}

	public GodPowerCreative getCreativePower()
	{
		return this.creativePower;
	}

	public GodPowerFill getFillPower()
	{
		return this.fillPower;
	}

	public GodPowerDelete getDeletePower()
	{
		return this.deletePower;
	}

	public GodPowerBlueprint getBlueprintPower()
	{
		return this.blueprintPower;
	}

	public GodPowerSelect getSelectPower()
	{
		return this.selectPower;
	}

	public IGodPower getCurrentPower()
	{
		return this.powers[this.currentPowerIndex];
	}

	public void setCurrentPower(final int powerIndex)
	{
		this.currentPowerIndex = powerIndex;
	}

	public void setCurrentPower(final Class<? extends IGodPower> clazz)
	{
		int foundIndex = -1;

		for (int i = 0; i < this.powers.length; i++)
		{
			final IGodPower p = this.powers[i];

			if (clazz.isAssignableFrom(p.getClass()))
			{
				foundIndex = i;
				break;
			}
		}

		if (foundIndex != -1)
		{
			this.currentPowerIndex = foundIndex;

			if (this.getWorld().isRemote)
			{
				OrbisCore.network().sendPacketToServer(new PacketChangePower(this.currentPowerIndex));
			}
		}
	}

	public int getCurrentPowerIndex()
	{
		return this.currentPowerIndex;
	}

	public int getPowerIndex(final Class<? extends IGodPower> clazz)
	{
		int foundIndex = -1;

		for (int i = 0; i < this.powers.length; i++)
		{
			final IGodPower p = this.powers[i];

			if (clazz.isAssignableFrom(p.getClass()))
			{
				foundIndex = i;
				break;
			}
		}

		return foundIndex;
	}

	public boolean isCurrentPower(final IGodPower power)
	{
		return this.getCurrentPower() == power;
	}

	public IGodPower[] array()
	{
		return this.powers;
	}

	@Override
	public void onUpdate()
	{
		for (final IGodPower power : this.powers)
		{
			power.onUpdate(this.getEntity(), this.getPlayer(), this.isCurrentPower(power));
		}

		final ItemStack stack = this.getEntity().getHeldItemMainhand();

		if (stack.getItem() instanceof ItemStackInput)
		{
			final ItemStackInput input = (ItemStackInput) stack.getItem();

			input.onUpdateInHand(this.getPlayer());
		}
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTTagCompound modules = new NBTTagCompound();

		for (final IGodPower power : this.powers)
		{
			power.write(modules);
		}

		tag.setTag("powers", modules);
		tag.setInteger("currentPowerIndex", this.currentPowerIndex);
		tag.setBoolean("scheduling", this.scheduling);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTTagCompound modules = tag.getCompoundTag("powers");

		for (final IGodPower power : this.powers)
		{
			power.read(modules);
		}

		this.currentPowerIndex = tag.getInteger("currentPowerIndex");
		this.scheduling = tag.getBoolean("scheduling");
	}

}
