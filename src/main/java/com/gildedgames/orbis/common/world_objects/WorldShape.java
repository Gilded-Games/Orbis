package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Simple wrapper around an IShape so it can be used as a WorldObject.
 * This is used in for example the Select tool for shared non-box shape rendering
 */
public class WorldShape implements IShape, IWorldObject, IColored
{
	private final List<IWorldObjectGroup> trackedGroups = Lists.newArrayList();

	private final World world;

	private IShape shape;

	private IWorldRenderer renderer;

	private WorldShape(final World world)
	{
		this.world = world;
	}

	public WorldShape(final IShape shape, final World world)
	{
		this.shape = shape;
		this.world = world;
	}

	@Override
	public void trackGroup(final IWorldObjectGroup group)
	{
		if (!this.trackedGroups.contains(group))
		{
			this.trackedGroups.add(group);
		}
	}

	@Override
	public void untrackGroup(final IWorldObjectGroup group)
	{
		this.trackedGroups.remove(group);
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public BlockPos getPos()
	{
		return this.shape.getBoundingBox().getMin();
	}

	/** TODO: Fix this dumb shit where we're recreating shapes just to translate them.
	 * To be honest, shapes shouldn't even contain position state. They should just be defining the
	 * actual.. well.. shape of the contained state between two points. That's it.
	 *
	 * All this position shit is horrible and should be moved to IWorldObject.
	 * @param pos
	 */
	@Override
	public void setPos(final BlockPos pos)
	{
		final BlockPos current = this.getPos();
		final int dx = pos.getX() - current.getX();
		final int dy = pos.getY() - current.getY();
		final int dz = pos.getZ() - current.getZ();
		this.shape = this.shape.translate(dx, dy, dz);
	}

	@Override
	public IShape getShape()
	{
		return this.shape;
	}

	@Override
	public IWorldRenderer getRenderer()
	{
		if (OrbisCore.isClient() && this.renderer == null)
		{
			final RenderShape r = new RenderShape(this);

			r.useCustomColors = true;

			r.colorGrid = this.getColor();
			r.colorBorder = this.getColor();

			this.renderer = r;
		}

		return this.renderer;
	}

	@Override
	public IData getData()
	{
		return null;
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> createShapeData()
	{
		return this.shape.createShapeData();
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> getShapeData()
	{
		return this.shape.getShapeData();
	}

	@Override
	public IShape rotate(final Rotation rotation, final IRegion in)
	{
		return this.shape.rotate(rotation, in);
	}

	@Override
	public IShape translate(final int x, final int y, final int z)
	{
		return this.shape.translate(x, y, z);
	}

	@Override
	public IShape translate(final BlockPos pos)
	{
		return this.shape.translate(pos);
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.shape.getBoundingBox();
	}

	@Override
	public boolean contains(final int x, final int y, final int z)
	{
		return this.shape.contains(x, y, z);
	}

	@Override
	public boolean contains(final BlockPos pos)
	{
		return this.shape.contains(pos);
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
		funnel.set("shape", this.shape);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
		this.shape = funnel.get(this.world, "shape");
	}

	@Override
	public int getColor()
	{
		return 0x999999;
	}
}
