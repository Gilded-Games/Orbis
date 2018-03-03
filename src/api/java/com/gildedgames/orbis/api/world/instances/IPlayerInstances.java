package com.gildedgames.orbis.api.world.instances;

import com.gildedgames.orbis.api.util.mc.BlockPosDimension;

public interface IPlayerInstances
{

	IInstance getInstance();

	void setInstance(IInstance instance);

	BlockPosDimension outside();

	void setOutside(BlockPosDimension pos);

}
