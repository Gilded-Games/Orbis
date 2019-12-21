package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintNetworkData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.BlueprintNetworkGenerator;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class PlayerBlueprintNetworkModule extends PlayerOrbisModule
{

	private List<BlueprintNetworkGenerator> generators = Lists.newArrayList();

	public PlayerBlueprintNetworkModule(final PlayerOrbis playerOrbis)
	{
		super(playerOrbis);
	}

	public void addNewNetwork(IDataIdentifier id, BlockPos pos)
	{
		BlueprintNetworkData network =
				new BlueprintNetworkData(5,
						Lists.newArrayList(id),
						Collections.emptyList(),
						Collections.emptyList(),
						Collections.emptyList());

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(this.getWorld()));
		BlueprintNetworkGenerator generator = new BlueprintNetworkGenerator(network, new CreationData(this.getWorld()).pos(pos), (r, c, z) -> {
		}, primer::place);

		this.generators.add(generator);
	}

	@Override
	public void onUpdate()
	{
		if (this.getPlayer().getEntity().ticksExisted % 3 == 0)
		{
			this.generators.removeIf(BlueprintNetworkGenerator::step);
		}
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
	}
}
