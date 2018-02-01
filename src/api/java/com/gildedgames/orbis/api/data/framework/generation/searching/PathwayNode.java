package com.gildedgames.orbis.api.data.framework.generation.searching;

import java.util.Iterator;
import java.util.Random;

import javax.annotation.Nullable;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.google.common.collect.AbstractIterator;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PathwayNode extends BlueprintRegion implements Node
{

	public final PathwayNode parent;

	public final BlockPos endConnection;

	private double h, g;

	public PathwayNode(PathwayNode parent, BlueprintRegion rect, BlockPos endConnection)
	{
		super(rect.getMin(), rect.getRotation(), rect.getData());
		this.parent = parent;

		this.endConnection = endConnection;
	}

	public Iterable<PathwayNode> fullPath()
	{
		return new Iterable<PathwayNode>()
		{
			@Override
			public Iterator<PathwayNode> iterator()
			{
				return new AbstractIterator<PathwayNode>()
				{
					PathwayNode node;

					@Override
					protected PathwayNode computeNext()
					{
						if (node == null)
						{
							node = PathwayNode.this;
							return node;
						}
						if (node.parent != null)
						{
							node = node.parent;
							return node;
						}
						return this.endOfData();
					}
				};
			}
		};
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PathwayNode))
			return false;

		BlockPos p = ((PathwayNode) obj).endConnection;
		return p.equals(this.endConnection);
	}

	@Override
	public int hashCode()
	{
		return endConnection.hashCode();
	}

	public EnumFacing sideOfConnection()
	{
		return PathwayUtil.sideOfConnection(this, this.endConnection);
	}

	@Override
	public void setG(double g)
	{
		this.g = g;
	}

	@Override
	public void setH(double h)
	{
		this.h = h;
	}

	@Override
	public double getG()
	{
		return this.g;
	}

	@Override
	public double getH()
	{
		return this.h;
	}

	@Override
	public double getF()
	{
		return this.getG() + this.getH();
	}

	@Override
	public int compareTo(Node o)
	{
		if(o.getF() == this.getF())
		{
//			return (new Random()).nextBoolean() ? 1 : -1;
			return Double.compare(this.getH(), o.getH());
		}
		return Double.compare(this.getF(), o.getF());
	}
}
