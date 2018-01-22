package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;

import java.util.*;

public class Graph<NODE,EDGE> implements NBT
{
	Set<NODE> vertices = new HashSet<>();

	Set<EDGE> edges = new HashSet<>();

	Map<NODE, Set<EDGE>> connections = new HashMap<>();

	Map<EDGE, Tuple<NODE, NODE>> edge_conn = new HashMap<>();

	public Set<NODE> vertexSet()
	{
		return this.vertices;
	}

	public Set<EDGE> edgesOf(NODE n)
	{
		return this.connections.get(n);
	}

	public Set<EDGE> edgeSet()
	{
		return this.edges;
	}

	public void removeEdge(EDGE edge1)
	{
		this.edges.remove(edge1);
		Tuple<NODE, NODE> nodes = this.edge_conn.get(edge1);
		this.connections.get(nodes.getFirst()).remove(edge1);
		this.connections.get(nodes.getSecond()).remove(edge1);
		this.edge_conn.remove(edge1);
	}

	public void addVertex(NODE n)
	{
		if (this.vertices.contains(n))
			return;
		this.vertices.add(n);
		this.connections.put(n, new HashSet<>());
	}

	public void addEdge(NODE node, NODE fdgdNode, EDGE nEdge1)
	{
		this.addVertex(node);
		this.connections.get(node).add(nEdge1);
		this.addVertex(fdgdNode);
		this.connections.get(fdgdNode).add(nEdge1);
		this.edges.add(nEdge1);
		this.edge_conn.put(nEdge1, new Tuple<>(node, fdgdNode));
	}

	public NODE getOpposite(NODE node, EDGE edge)
	{
		Tuple<NODE, NODE> nodes = this.edge_conn.get(edge);
		if (nodes.getFirst() == node)
			return nodes.getSecond();
		if (nodes.getSecond() == node)
			return nodes.getFirst();
		return null;
	}

	public boolean canReach(NODE n1, NODE n2)
	{
		Set<NODE> visitedNodes = new HashSet<>();
		Stack<NODE> unvisitedNodes = new Stack<>();
		unvisitedNodes.add(n2);
		while(unvisitedNodes.size() > 0)
		{
			NODE activeNode = unvisitedNodes.pop();
			if(activeNode == n1)
				return true;
			for (EDGE e : this.edgesOf(activeNode))
			{
				NODE n = this.getOpposite(activeNode, e);
				if(!visitedNodes.contains(n))
					unvisitedNodes.add(n);
			}
			visitedNodes.add(activeNode);
		}
		return false;
	}

	public boolean containsVertex(NODE node1)
	{
		return this.vertices.contains(node1);
	}

	public EDGE getEdge(NODE node1, NODE node2)
	{
		for(EDGE e1 : this.connections.get(node1))
			for(EDGE e2 : this.connections.get(node2))
				if(e1 == e2)
					return e1;
		return null;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		//TODO
	}

	@Override
	public void read(NBTTagCompound tag)
	{

	}
}
