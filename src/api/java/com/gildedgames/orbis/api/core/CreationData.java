package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Random;

public class CreationData implements ICreationData<CreationData>
{

	private Random rand;

	private long seed;

	private BlockPos pos = BlockPos.ORIGIN;

	private World world;

	private EntityPlayer creator;

	private Rotation rotation = Rotation.NONE;

	private boolean placeAir = true, schedules = false, placesVoid = false;

	private int dimId;

	protected CreationData()
	{

	}

	public CreationData(final World world)
	{
		this.world = world;

		if (world != null)
		{
			this.rand = world.rand;
		}
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
	public CreationData placesVoid(boolean placesVoid)
	{
		this.placesVoid = placesVoid;

		return this;
	}

	@Override
	public CreationData pos(final BlockPos pos)
	{
		this.pos = pos;

		return this;
	}

	@Override
	public CreationData world(final World world)
	{
		this.world = world;

		if (this.world != null)
		{
			this.dimId = this.world.provider.getDimension();
		}

		return this;
	}

	@Override
	public CreationData rotation(final Rotation rotation)
	{
		this.rotation = rotation;

		return this;
	}

	@Override
	public CreationData seed(long seed)
	{
		this.seed = seed;

		this.rand = new Random(seed);

		return this;
	}

	@Override
	public CreationData creator(final EntityPlayer creator)
	{
		this.creator = creator;

		return this;
	}

	@Override
	public CreationData placesAir(final boolean placeAir)
	{
		this.placeAir = placeAir;

		return this;
	}

	@Override
	public CreationData schedules(final boolean schedules)
	{
		this.schedules = schedules;

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
		if (this.world == null)
		{
			this.world = DimensionManager.getWorld(this.dimId);
		}

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
	public boolean placesVoid()
	{
		return this.placesVoid;
	}

	@Override
	public ICreationData clone()
	{
		CreationData data = new CreationData(this.world).pos(new BlockPos(this.pos)).rotation(this.rotation).creator(this.creator).placesAir(this.placeAir)
				.schedules(this.schedules).placesVoid(this.placesVoid);

		data.seed = this.seed;
		data.rand = this.rand;

		return data;
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
		tag.setBoolean("placesVoid", this.placesVoid);
		tag.setBoolean("schedules", this.schedules);
		tag.setLong("seed", this.seed);

		if (this.world != null)
		{
			tag.setInteger("dimId", this.world.provider.getDimension());
		}
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		this.pos = NBTHelper.readBlockPos(tag.getCompoundTag("pos"));
		this.rotation = Rotation.valueOf(tag.getString("rotation"));
		this.placeAir = tag.getBoolean("placeAir");
		this.placesVoid = tag.getBoolean("placesVoid");
		this.schedules = tag.getBoolean("schedules");
		this.seed = tag.getLong("seed");

		if (tag.hasKey("dimId"))
		{
			this.dimId = tag.getInteger("dimId");
		}

		this.rand = new Random(this.seed);
	}
}
