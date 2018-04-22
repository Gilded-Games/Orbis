package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.client.renderers.framework.RenderFrameworkEditing;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.FrameworkNode;
import com.gildedgames.orbis_api.data.framework.IFrameworkDataListener;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.pathway.PathwayData;
import com.gildedgames.orbis_api.data.region.*;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Framework extends AbstractRegion implements IWorldObject, IColored, IMutableRegion, IRotateable, IFrameworkDataListener
{
	protected Rotation rotation = Rotation.NONE;

	protected BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	private World world;

	private IWorldRenderer renderer;

	private FrameworkData data;

	private boolean isDirty;

	private Framework()
	{
		this.data = new FrameworkData(0, 0, 0);
		this.data.setWorldObjectParent(this);
		this.data.listen(this);
	}

	private Framework(World world)
	{
		this();

		this.world = world;
	}

	public Framework(World world, final IRegion region)
	{
		this(world);

		this.data = new FrameworkData(region.getWidth(), region.getHeight(), region.getLength());
		this.data.setWorldObjectParent(this);
		this.data.listen(this);

		this.setBounds(region);
	}

	public Framework(World world, FrameworkData data)
	{
		this(world);

		this.data = data;
		this.data.setWorldObjectParent(this);
		this.data.listen(this);

		this.setBounds(new Region(BlockPos.ORIGIN, new BlockPos(data.getWidth(), data.getHeight(), data.getLength())));
	}

	public Collection<IFrameworkNode> findIntersectingNodes(IShape s)
	{
		List<IFrameworkNode> nodes = Lists.newArrayList();

		for (Map.Entry<Pair<Integer, IFrameworkNode>, BlockPos> entry : this.data.getNodeToPosMap().entrySet())
		{
			IFrameworkNode node = entry.getKey().getValue();
			BlockPos p = entry.getValue();

			int minX = p.getX() + this.getPos().getX();
			int minY = p.getY() + this.getPos().getY();
			int minZ = p.getZ() + this.getPos().getZ();

			int maxX = minX + node.getBounds().getWidth() - 1;
			int maxY = minY + node.getBounds().getHeight() - 1;
			int maxZ = minZ + node.getBounds().getLength() - 1;

			IRegion r = s.getBoundingBox();

			if (maxX >= r.getBoundingBox().getMin().getX() && minX <= r.getMax().getX() && maxY >= r.getMin().getY() && minY <= r.getMax().getY()
					&& maxZ >= r.getMin().getZ() && minZ <= r.getMax().getZ())
			{
				nodes.add(node);
			}
		}

		return nodes;
	}

	public IFrameworkNode findIntersectingNode(BlockPos pos)
	{
		for (Map.Entry<Pair<Integer, IFrameworkNode>, BlockPos> entry : this.data.getNodeToPosMap().entrySet())
		{
			IFrameworkNode node = entry.getKey().getValue();
			BlockPos p = entry.getValue();

			int minX = p.getX() + this.getPos().getX();
			int minY = p.getY() + this.getPos().getY();
			int minZ = p.getZ() + this.getPos().getZ();

			int maxX = minX + node.getBounds().getWidth() - 1;
			int maxY = minY + node.getBounds().getHeight() - 1;
			int maxZ = minZ + node.getBounds().getLength() - 1;

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
		this.setBounds(region.getMin(), region.getMax());
	}

	@Override
	public void setBounds(final BlockPos corner1, final BlockPos corner2)
	{
		this.min = RegionHelp.getMin(corner1, corner2);
		this.max = RegionHelp.getMax(corner1, corner2);

		this.notifyDataChange();
		this.isDirty = true;
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
		this.isDirty = true;
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
	public void onUpdate()
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("min", this.min);

		tag.setString("rotation", this.rotation.name());

		funnel.set("data", this.data);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.min = funnel.getPos("min");

		this.rotation = Rotation.valueOf(tag.getString("rotation"));

		this.data = funnel.getWithDefault("data", this::getData);

		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.data.setWorldObjectParent(this);
		this.data.listen(this);

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

	@Override
	public void onAddNode(IFrameworkNode node)
	{
		this.isDirty = true;
	}

	@Override
	public void onRemoveNode(IFrameworkNode node)
	{
		this.isDirty = true;
	}

	@Override
	public void onAddEdge(FrameworkNode n1, FrameworkNode n2)
	{
		this.isDirty = true;
	}

	@Override
	public void onAddIntersection(PathwayData pathway1, PathwayData pathway2, BlueprintData blueprint)
	{
		this.isDirty = true;
	}
}
