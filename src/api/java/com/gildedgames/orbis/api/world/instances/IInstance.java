package com.gildedgames.orbis.api.world.instances;

import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.DimensionType;

import java.util.List;

public interface IInstance extends NBT
{

	void onJoin(EntityPlayer player);

	void onLeave(EntityPlayer player);

	List<EntityPlayer> getPlayers();

	DimensionType getDimensionType();

	int getDimensionId();

	void setDimensionId(int dimensionId);

}
