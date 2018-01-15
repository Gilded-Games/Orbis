package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class CreationData implements ICreationData
{

	private Random rand;

	private BlockPos pos;

	private World world;

	private EntityPlayer creator;

	private Rotation rotation = Rotation.NONE;

	private boolean placeAir = true, schedules = false, erases = false;

	public CreationData(final World world)
	{
		this.world = world;
		this.rand = world.rand;
	}

	public CreationData(final World world, final long seed)
	{
		this.world = world;
		this.rand = new Random(seed);
	}

	public CreationData(final World world, final EntityPlayer creator)
	{
		this(world);

		this.creator = creator;
	}

	@Override
	public ICreationData pos(final BlockPos pos)
	{
		this.pos = pos;

		return this;
	}

	@Override
	public ICreationData world(final World world)
	{
		this.world = world;

		return this;
	}

	@Override
	public ICreationData rotation(final Rotation rotation)
	{
		this.rotation = rotation;

		return this;
	}

	@Override
	public ICreationData rand(final Random random)
	{
		this.rand = random;

		return this;
	}

	@Override
	public ICreationData creator(final EntityPlayer creator)
	{
		this.creator = creator;

		return this;
	}

	@Override
	public ICreationData placesAir(final boolean placeAir)
	{
		this.placeAir = placeAir;

		return this;
	}

	@Override
	public ICreationData schedules(final boolean schedules)
	{
		this.schedules = schedules;

		return this;
	}

	@Override
	public ICreationData erases(final boolean erases)
	{
		this.erases = erases;

		return this;
	}

	@Override
	public BlockPos getPos()
	{
		return this.pos;
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public Random getRandom()
	{
		return this.rand;
	}

	@Override
	public Rotation getRotation()
	{
		return this.rotation;
	}

	@Override
	public EntityPlayer getCreator()
	{
		return this.creator;
	}

	@Override
	public boolean placeAir()
	{
		return this.placeAir;
	}

	@Override
	public boolean schedules()
	{
		return this.schedules;
	}

	@Override
	public boolean erases()
	{
		return this.erases;
	}

	@Override
	public ICreationData clone()
	{
		return new CreationData(this.world).pos(new BlockPos(this.pos)).rand(this.rand).rotation(this.rotation).creator(this.creator).placesAir(this.placeAir)
				.schedules(this.schedules).erases(this.erases);
	}

	@Override
	public boolean shouldCreate(final BlockData data, final BlockPos pos)
	{
		return true;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		tag.setTag("pos", NBTHelper.writeBlockPos(this.pos));
		tag.setString("rotation", this.rotation.name());
		tag.setBoolean("placeAir", this.placeAir);
		tag.setBoolean("schedules", this.schedules);
		tag.setBoolean("erases", this.erases);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		this.pos = NBTHelper.readBlockPos(tag.getCompoundTag("pos"));
		this.rotation = Rotation.valueOf(tag.getString("rotation"));
		this.placeAir = tag.getBoolean("placeAir");
		this.schedules = tag.getBoolean("schedules");
		this.erases = tag.getBoolean("erases");
	}
}
