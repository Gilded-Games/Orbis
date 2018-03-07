package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.region.IColored;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WorldRegion extends Region implements IWorldObject, IColored
{
	private IWorldRenderer renderer;

	private World world;

	private boolean isDirty;

	protected WorldRegion(final World world)
	{
		super();

		this.world = world;
	}

	public WorldRegion(final IRegion region, final World world)
	{
		super(region);

		this.world = world;
	}

	public WorldRegion(final BlockPos pos, final World world)
	{
		super(pos);

		this.world = world;
	}

	public WorldRegion(final BlockPos corner1, final BlockPos corner2, final World world)
	{
		super(corner1, corner2);
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.getMin().hashCode());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		final boolean flag = super.equals(o);

		if (flag)
		{
			return true;
		}

		if (!(o instanceof Region))
		{
			return false;
		}

		final WorldRegion region = (WorldRegion) o;

		if (this.getMin().getX() == region.getMin().getX() && this.getMax().getX() == region.getMax().getX() && this.getMin().getY() == region.getMin().getY()
				&& this.getMax().getY() == region.getMax().getY() && this.getMin().getZ() == region.getMin().getZ() && this.getMax().getZ() == region.getMax()
				.getZ())
		{
			return this.getWorld().equals(region.getWorld());
		}

		return false;
	}

	@Override
	public void markClean()
	{
		this.isDirty = false;
	}

	@Override
	public boolean isDirty()
	{
		return this.isDirty;
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public BlockPos getPos()
	{
		return this.getMin();
	}

	@Override
	public void setPos(final BlockPos pos)
	{
		this.translate(pos);
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
	public void write(final NBTTagCompound tag)
	{
		super.write(tag);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		super.read(tag);
	}

	@Override
	public int getColor()
	{
		return 0x999999;
	}
}
