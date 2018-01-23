package com.gildedgames.orbis.api.data.framework.generation;

import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.data.framework.Graph;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class FDGenUtil
{
	public static Region boundingBox(Graph<FDGDNode, FDGDEdge> graph)
	{
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (FDGDNode n : graph.vertexSet())
		{
			BlockPos min = n.getMin();
			BlockPos max = n.getMax();
			minX = Math.min(minX, min.getX());
			minY = Math.min(minY, min.getY());
			minZ = Math.min(minZ, min.getZ());

			maxX = Math.max(maxX, max.getX());
			maxY = Math.max(maxY, max.getY());
			maxZ = Math.max(maxZ, max.getZ());
		}
		return new Region(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
	}

	public static boolean hasCollision(Graph<FDGDNode, FDGDEdge> graph)
	{
		for (final FDGDNode node1 : graph.vertexSet())
			for (final FDGDNode node2 : graph.vertexSet())
				if (node1 != node2 && RegionHelp.intersects(node1, node2))
					return true;
		return false;
	}

	private static float ESCAPE_DIST = 5f;
	public static float[] pointOfForce(FDGDNode v, FDGDNode u)
	{
		//WATCH OUT: The 0 here is wrong.
		float vPosX = v.getX(), vPosZ = v.getZ();
		float uPosX = 0, uPosZ = 0;
		if(RegionHelp.contains(u, vPosX, 0, vPosZ))
		{
			// Find the vector between the center of gravity of both bodies.
			// Then, move the center of gravity ESCAPE_DIST close to the body we are
			// going to apply the force onto.
			float dx = vPosX - u.getX();
			float dz = vPosZ - u.getZ();
			float length = (float) Math.sqrt(dx * dx + dz * dz);
			dx = dx / length * ESCAPE_DIST;
			dz = dz / length * ESCAPE_DIST;
			uPosX = vPosX - dx;
			uPosZ = vPosZ - dz;
		}
		else if (RegionHelp.intersects(v, u))
		{
			float edgeClampX = MathHelper.clamp(vPosX, u.getMin().getX(), u.getMax().getX());
			float edgeClampZ = MathHelper.clamp(vPosZ, u.getMin().getZ(), u.getMax().getZ());
			float dx = vPosX - edgeClampX;
			float dz = vPosZ - edgeClampZ;
			float length = (float) Math.sqrt(dx * dx + dz * dz);
			dx = dx / length * ESCAPE_DIST;
			dz = dz / length * ESCAPE_DIST;
			uPosX = vPosX - dx;
			uPosZ = vPosZ - dz;
		}
		else
		{
//			uPosX = MathHelper.clamp(vPosX, u.getMin().getX(), u.getMax().getX());
//			uPosZ = MathHelper.clamp(vPosZ, u.getMin().getZ(), u.getMax().getZ());
			float edgeClampUX = MathHelper.clamp(vPosX, u.getMin().getX(), u.getMax().getX());
			float edgeClampUZ = MathHelper.clamp(vPosZ, u.getMin().getZ(), u.getMax().getZ());
			float edgeClampVX = MathHelper.clamp(edgeClampUX, v.getMin().getX(), v.getMax().getX());
			float edgeClampVZ = MathHelper.clamp(edgeClampUZ, v.getMin().getZ(), v.getMax().getZ());
			float dx = vPosX - edgeClampUX;
			float dz = vPosZ - edgeClampUZ;
			float dxClamp = edgeClampVX - edgeClampUX;
			float dzClamp = edgeClampVZ - edgeClampUZ;
			float targetLength = (float) Math.sqrt(dxClamp * dxClamp + dzClamp * dzClamp);
			float length = (float) Math.sqrt(dx * dx + dz * dz);
			dx = dx / length * targetLength;
			dz = dz / length * targetLength;
			uPosX = vPosX - dx;
			uPosZ = vPosZ - dz;
		}
		return new float[]{uPosX, 0, uPosZ};
	}

	public static boolean hasEdgeIntersections(Graph<FDGDNode, FDGDEdge> graph)
	{
		final List<FDGDEdge> edges = new ArrayList<>(graph.edgeSet());

		for (int i = 0; i < edges.size(); i++)
			for (int x = i + 1; x < edges.size(); x++)
			{
				final FDGDEdge edge1 = edges.get(i);
				final FDGDEdge edge2 = edges.get(x);

				final FDGDNode e1S = edge1.node1();
				final FDGDNode e1T = edge1.node2();

				final FDGDNode e2S = edge2.node1();
				final FDGDNode e2T = edge2.node2();

				if (e1T != e2T && e1T != e2S && e1S != e2T && e1S != e2S)
					if (isIntersecting(edge1, edge2))
						return true;
			}
		return false;
	}

	public static boolean hasEdgeIntersections(Graph<FDGDNode, FDGDEdge> graph, FDGDEdge edge)
	{
		for (FDGDEdge e : graph.edgeSet())
			if (isIntersecting(edge, e))
				return true;
		return false;
	}

	// Returns true if the two edges have an intersection somewhere.
	public static boolean isIntersecting(FDGDEdge edge1, FDGDEdge edge2)
	{
		final float line1X = edge1.node2().getX() - edge1.node1().getX();
		final float line1Z = edge1.node2().getZ() - edge1.node1().getZ();

		final float line2X = edge2.node2().getX() - edge2.node1().getX();
		final float line2Z = edge2.node2().getZ() - edge2.node1().getZ();

		final float diffX = edge1.node1().getX() - edge2.node1().getX();
		final float diffZ = edge1.node1().getZ() - edge2.node1().getZ();

		//e1s, e1t, e2s, e2t
//		final float line1X = edge1.entrance2X() - edge1.entrance1X();
//		final float line1Z = edge1.entrance2Z() - edge1.entrance1Z();
//
//		final float line2X = edge2.entrance2X() - edge2.entrance1X();
//		final float line2Z = edge2.entrance2Z() - edge2.entrance1Z();
//
//		final float diffX = edge1.entrance1X() - edge2.entrance1X();
//		final float diffZ = edge1.entrance1Z() - edge2.entrance1Z();
		final float denom = line1X * line2Z - line2X * line1Z;

		final float s = (line1X * diffZ - line1Z * diffX) / denom;
		final float t = (line2X * diffZ - line2Z * diffX) / denom;

		return s > 0 && s < 1 && t > 0 && t < 1;

	}

	public static int euclidian(BlockPos from, BlockPos to)
	{
		return euclidian(from, to.getX(), to.getY(), to.getZ());
	}

	public static int euclidian(BlockPos from, int toX, int toY, int toZ)
	{
		return euclidian(from.getX(), from.getY(), from.getZ(), toX, toY, toZ);
	}

	public static int euclidian(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
	{
		return Math.abs(fromX - toX) + Math.abs(fromY - toY) + Math.abs(fromZ - toZ);
	}

	public static float euclidian(float fromX, float fromY, float fromZ, float toX, float toY, float toZ)
	{
		return Math.abs(fromX - toX) + Math.abs(fromY - toY) + Math.abs(fromZ - toZ);
	}
}
