package com.gildedgames.orbis.api.data.framework.generation.csp;

import net.minecraft.util.Tuple;

import java.util.*;

public class Multigraph<NODE, EDGE>
{
    Set<NODE> vertices = new HashSet<>();

    Set<EDGE> edges = new HashSet<>();

    Map<NODE, Set<EDGE>> connections = new HashMap<>();

    Map<EDGE, Set<NODE>> edge_conn = new HashMap<>();

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
        Set<NODE> nodes = this.edge_conn.get(edge1);
        for (NODE n : nodes)
            this.connections.get(n).remove(edge1);
        this.edge_conn.remove(edge1);
    }

    public void addVertex(NODE n)
    {
        this.vertices.add(n);
        this.connections.put(n, new HashSet<>());
    }

    public void addEdge(Collection<NODE> nodes, EDGE nEdge1)
    {
        for (NODE n : nodes)
        {
            this.addVertex(n);
            this.connections.get(n).add(nEdge1);
        }
        this.edges.add(nEdge1);
        this.edge_conn.put(nEdge1, new HashSet<>(nodes));
    }

    public boolean containsVertex(NODE node1)
    {
        return this.vertices.contains(node1);
    }

    public Set<EDGE> getEdges(NODE node1, NODE node2)
    {
        Set<EDGE> edges = new HashSet<EDGE>();
        for(EDGE e1 : this.connections.get(node1))
            for(EDGE e2 : this.connections.get(node2))
                if(e1 == e2)
                    edges.add(e1);
        return edges;
    }
}
