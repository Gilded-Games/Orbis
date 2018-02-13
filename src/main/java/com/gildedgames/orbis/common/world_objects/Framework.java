package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.region.*;
import com.gildedgames.orbis.api.util.RegionHelp;
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

public class Framework extends AbstractRegion implements IWorldObject, IColored, IMutableRegion, IRotateable
{
	private final List<IWorldObjectGroup> trackedGroups = Lists.newArrayList();

	protected Rotation rotation = Rotation.NONE;

	protected BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	private World world;

	private int width, height, length;

	private IWorldRenderer renderer;

	public Framework(World world, final IRegion region)
	{
		this.world = world;
		this.setBounds(region);
	}

	public Framework(World world, final BlockPos pos, final BlueprintData data)
	{
		this.world = world;
		this.setPos(pos);
	}

	public Framework(World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		this.world = world;
		this.rotation = rotation;

		this.setPos(pos);
	}

	@Override
	public void setBounds(final IRegion region)
	{
		this.min = region.getMin();
		this.max = region.getMax();

		this.notifyDataChange();
	}

	@Override
	public void setBounds(final BlockPos corner1, final BlockPos corner2)
	{
		this.min = RegionHelp.getMin(corner1, corner2);
		this.max = RegionHelp.getMax(corner1, corner2);

		this.notifyDataChange();
	}

	@Override
	public BlockPos getPos()
	{
		return this.min;
	}

	@Override
	public void setPos(final BlockPos pos)
	{
		this.min = pos;
		int width = this.rotation == Rotation.NONE || this.rotation == Rotation.CLOCKWISE_180 ? this.getWidth() : this.getLength();
		int length = this.rotation == Rotation.NONE || this.rotation == Rotation.CLOCKWISE_180 ? this.getLength() : this.getWidth();
		this.max = RegionHelp.getMax(this.min, width, this.getHeight(), length);

		this.width = RegionHelp.getWidth(this.min, this.max);
		this.height = RegionHelp.getHeight(this.min, this.max);
		this.length = RegionHelp.getLength(this.min, this.max);

		this.notifyDataChange();
	}

	@Override
	public BlockPos getMin()
	{
		return this.min;
	}

	@Override
	public BlockPos getMax()
	{
		return this.max;
	}

	@Override
	public int getWidth()
	{
		return this.width;
	}

	@Override
	public int getHeight()
	{
		return this.height;
	}

	@Override
	public int getLength()
	{
		return this.length;
	}

	@Override
	public void trackGroup(IWorldObjectGroup group)
	{
		if (!this.trackedGroups.contains(group))
		{
			this.trackedGroups.add(group);
		}
	}

	@Override
	public void untrackGroup(IWorldObjectGroup group)
	{
		this.trackedGroups.remove(group);
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public IShape getShape()
	{
		return this;
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
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("min", this.min);

		tag.setString("rotation", this.rotation.name());
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.min = funnel.getPos("min");

		this.rotation = Rotation.valueOf(tag.getString("rotation"));

		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.notifyDataChange();
	}

	@Override
	public int getColor()
	{
		return 0x9f73d4;
	}

	@Override
	public Rotation getRotation()
	{
		return this.rotation;
	}
}
