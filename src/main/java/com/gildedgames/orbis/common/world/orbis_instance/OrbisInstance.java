package com.gildedgames.orbis.common.world.orbis_instance;

import com.gildedgames.orbis.api.util.mc.BlockPosDimension;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import com.gildedgames.orbis.api.world.instances.IInstance;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;

import java.util.List;

public class OrbisInstance implements IInstance
{

	private final List<EntityPlayer> players = Lists.newArrayList();

	private BlockPosDimension outsideEntrance, insideEntrance;

	private int dimensionId;

	@SuppressWarnings("unused")
	private OrbisInstance()
	{

	}

	public OrbisInstance(final int id)
	{
		this.dimensionId = id;

		this.insideEntrance = new BlockPosDimension(0, 2, 0, this.dimensionId);
	}

	public BlockPosDimension getOutsideEntrance()
	{
		return this.outsideEntrance;
	}

	public void setOutsideEntrance(final BlockPosDimension entrance)
	{
		this.outsideEntrance = entrance;
	}

	public BlockPosDimension getInsideEntrance()
	{
		return this.insideEntrance;
	}

	public void setInsideEntrance(final BlockPosDimension entrance)
	{
		this.insideEntrance = entrance;
	}

	@Override
	public void write(final NBTTagCompound output)
	{
		output.setTag("outsideEntrance", NBTHelper.write(this.outsideEntrance));
		output.setTag("insideEntrance", NBTHelper.write(this.insideEntrance));

		output.setInteger("dimensionId", this.dimensionId);
	}

	@Override
	public void read(final NBTTagCompound input)
	{
		this.outsideEntrance = NBTHelper.read(input.getCompoundTag("outsideEntrance"));
		this.insideEntrance = NBTHelper.read(input.getCompoundTag("insideEntrance"));

		this.dimensionId = input.getInteger("dimensionId");
	}

	@Override
	public void onJoin(final EntityPlayer player)
	{
		this.players.add(player);
	}

	@Override
	public void onLeave(final EntityPlayer player)
	{
		this.players.remove(player);
	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		return this.players;
	}

	@Override
	public DimensionType getDimensionType()
	{
		return WorldProviderOrbis.ORBIS;
	}

	@Override
	public int getDimensionId()
	{
		return this.dimensionId;
	}

	@Override
	public void setDimensionId(int dimensionId)
	{
		this.dimensionId = dimensionId;
	}

}
