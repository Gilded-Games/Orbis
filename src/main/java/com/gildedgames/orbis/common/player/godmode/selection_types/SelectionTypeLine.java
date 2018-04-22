package com.gildedgames.orbis.common.player.godmode.selection_types;

import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.shapes.LineShape;
import com.gildedgames.orbis.client.godmode.selection_types.ISelectionTypeClient;
import com.gildedgames.orbis.client.godmode.selection_types.SelectionTypeClientLine;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SelectionTypeLine implements ISelectionType
{
	private ISelectionTypeClient client;

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Override
	public ISelectionTypeClient getClient()
	{
		if (OrbisCore.isClient() && this.client == null)
		{
			this.client = new SelectionTypeClientLine();
		}

		return this.client;
	}

	@Override
	public IShape createShape(final BlockPos start, final BlockPos end, final PlayerOrbis playerOrbis, final boolean centered)
	{
		return new LineShape(start, end, 1);
	}
}
