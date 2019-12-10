package com.gildedgames.orbis.common.player.godmode.selection_types;

import com.gildedgames.orbis.client.godmode.selection_types.ISelectionTypeClient;
import com.gildedgames.orbis.client.godmode.selection_types.SelectionTypeClientSphere;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.data.shapes.SphereShape;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SelectionTypeSphere implements ISelectionType
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
			this.client = new SelectionTypeClientSphere();
		}

		return this.client;
	}

	@Override
	public IShape createShape(final BlockPos start, final BlockPos end, final PlayerOrbis playerOrbis, final boolean centered)
	{
		return new SphereShape(start, end, centered);
	}
}
