package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.client.renderers.RenderGhostBlockDataContainer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.region.*;
import com.gildedgames.orbis.lib.util.RegionHelp;
import com.gildedgames.orbis.lib.util.RotationHelp;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.IWorldRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;

public class GhostBlockDataContainer extends AbstractRegion implements IMutableRegion, IRotateable, IWorldObject, IColored
{
	protected Rotation rotation = Rotation.NONE;

	protected BlockDataContainer data;

	protected BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	private IWorldRenderer renderer;

	private World world;

	private boolean isDirty;

	protected GhostBlockDataContainer()
	{

	}

	protected GhostBlockDataContainer(final World world)
	{
		this.world = world;
	}

	public GhostBlockDataContainer(World world, final IRegion region)
	{
		this.world = world;
		this.data = new BlockDataContainer(region);

		this.setBounds(region);
	}

	public GhostBlockDataContainer(World world, final BlockPos pos, final BlockDataContainer data)
	{
		this.world = world;
		this.data = data;

		this.setPos(pos);
	}

	public GhostBlockDataContainer(final BlockPos pos, final Rotation rotation, final BlockDataContainer data)
	{
		this.data = data;
		this.rotation = rotation;

		this.setPos(pos);
	}

	@Override
	public Rotation getRotation()
	{
		return this.rotation;
	}

	public IBlockState getBlock(final BlockPos pos)
	{
		final BlockPos transformed = this.transformForRegion(pos);
		return this.getBlockDataContainer().getBlockState(transformed);
	}

	public BlockPos transformForRegion(final BlockPos pos)
	{
		final Rotation transformRot =
				this.rotation == Rotation.CLOCKWISE_90 ?
						Rotation.COUNTERCLOCKWISE_90 :
						this.rotation == Rotation.COUNTERCLOCKWISE_90 ? Rotation.CLOCKWISE_90 : this.rotation;
		final BlockPos rotated = RotationHelp.rotate(pos, this, transformRot);
		final IRegion rotatedRegion = RotationHelp.rotate(this, transformRot);
		return new BlockPos(rotated.getX() - rotatedRegion.getMin().getX(), rotated.getY() - rotatedRegion.getMin().getY(),
				rotated.getZ() - rotatedRegion.getMin().getZ());
	}

	@Override
	public int getWidth()
	{
		return this.data.getWidth();
	}

	@Override
	public int getHeight()
	{
		return this.data.getHeight();
	}

	@Override
	public int getLength()
	{
		return this.data.getLength();
	}

	@Override
	public void markDirty()
	{
		this.isDirty = true;
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
	public void setWorld(World world)
	{
		this.world = world;
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

		this.notifyDataChange();
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
			this.renderer = new RenderGhostBlockDataContainer(this);
		}

		return this.renderer;
	}

	@Override
	public IData getData()
	{
		return this.data;
	}

	@Override
	public void onUpdate()
	{

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
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("min", this.min);

		tag.setString("rotation", this.rotation.name());

		funnel.set("state", this.data);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.min = funnel.getPos("min");

		this.rotation = Rotation.valueOf(tag.getString("rotation"));

		this.data = funnel.get("state");

		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.notifyDataChange();
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

	public BlockDataContainer getBlockDataContainer()
	{
		return this.data;
	}

	@Override
	public int getColor()
	{
		return 0xFFFFFF;
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.min.hashCode());

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

		if (!(o instanceof GhostBlockDataContainer))
		{
			return false;
		}

		final GhostBlockDataContainer b = (GhostBlockDataContainer) o;

		if (this.getMin().getX() == b.getMin().getX() && this.getMax().getX() == b.getMax().getX() && this.getMin().getY() == b.getMin().getY()
				&& this.getMax().getY() == b.getMax().getY() && this.getMin().getZ() == b.getMin().getZ() && this.getMax().getZ() == b.getMax()
				.getZ() && this.data == b.data)
		{
			return this.getWorld().equals(b.getWorld());
		}

		return false;
	}

	@Override
	public Iterator<BlockPos.MutableBlockPos> iterator()
	{
		return BlockPos.getAllInBoxMutable(this.min, this.max).iterator();
	}
}
