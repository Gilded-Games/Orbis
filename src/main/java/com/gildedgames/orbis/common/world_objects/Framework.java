package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.region.*;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.framework.RenderFrameworkEditing;
import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

public class Framework extends AbstractRegion implements IWorldObject, IColored, IMutableRegion, IRotateable
{
	protected Rotation rotation = Rotation.NONE;

	protected BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	private World world;

	private int width, height, length;

	private IWorldRenderer renderer;

	private FrameworkData data;

	private boolean isDirty;

	private Framework()
	{
		this.data = new FrameworkData(300, 300, 300);
	}

	public Framework(World world, final IRegion region)
	{
		this();
		this.world = world;
		this.setBounds(region);
	}

	public Framework(World world, final BlockPos pos, final BlueprintData data)
	{
		this();
		this.world = world;
		this.setPos(pos);
	}

	public Framework(World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		this();
		this.world = world;
		this.rotation = rotation;

		this.setPos(pos);
	}

	public IFrameworkNode findIntersectingNode(BlockPos pos)
	{
		for (Map.Entry<IFrameworkNode, BlockPos> entry : this.data.getNodeToPosMap().entrySet())
		{
			IFrameworkNode node = entry.getKey();
			BlockPos p = entry.getValue();

			int minX = p.getX() + this.getPos().getX();
			int minY = p.getY() + this.getPos().getY();
			int minZ = p.getZ() + this.getPos().getZ();

			int maxX = minX + node.largestPossibleDim().getWidth() - 1;
			int maxY = minY + node.largestPossibleDim().getHeight() - 1;
			int maxZ = minZ + node.largestPossibleDim().getLength() - 1;

			if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ)
			{
				return node;
			}
		}

		return null;
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
	public IShape getShape()
	{
		return this;
	}

	@Override
	public IWorldRenderer getRenderer()
	{
		if (OrbisCore.isClient() && this.renderer == null)
		{
			final RenderFrameworkEditing r = new RenderFrameworkEditing(this);

			this.renderer = r;
		}

		return this.renderer;
	}

	@Override
	public FrameworkData getData()
	{
		return this.data;
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

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.min.hashCode());

		return builder.toHashCode();
	}
}
