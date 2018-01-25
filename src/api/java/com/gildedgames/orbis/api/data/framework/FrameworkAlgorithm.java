package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.data.framework.generation.FDGenUtil;
import com.gildedgames.orbis.api.data.framework.generation.FailedToGenerateException;
import com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms.FruchtermanReingold;
import com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms.IGDAlgorithm;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayProblem;
import com.gildedgames.orbis.api.data.framework.generation.searching.StepAStar;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.Region;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class FrameworkAlgorithm
{
	// How quickly the algorithm adds nodes on intersections of edges
	private final static float spiderwebGrowth = 0.5f, graphDestroy = 0.3f;

	// We can only add edges during the create/destroy phase when
	// the two nodes are within addEdgeDistanceRatio of the
	// length of the diagonal of the surrounding boundingbox.
	private final static float addEdgeDistanceRatio = 0.4f;

	public static float heuristicWeight = 3;

	private final FrameworkData framework;

	private Graph<FDGDNode, FDGDEdge> fdgdGraph;

	public Map<FrameworkNode, FDGDNode> _nodeMap; // Used for debugging

	private Phase phase = Phase.CSP;

	private boolean escapePhase;

	private int fdgdIterations = 0;

	private World world;

	private StepAStar<PathwayNode> pathfindingSolver;

	//	private StepAStar<PathwayNode> pathfindingSolver;

	private Iterator<FDGDEdge> edgeIterator;

	//	private List<FrameworkFragment> fragments;

	private Random random;

	private IGDAlgorithm gdAlgorithm = new FruchtermanReingold();

	private List<BlueprintRegion> fragments;

	public FrameworkAlgorithm(FrameworkData data, World world)
	{
		this.framework = data;
		this.world = world;
		this.random = world == null ? new Random() : world.rand;
	}

	public GeneratedFramework computeFully() throws FailedToGenerateException
	{
		while (!this.step())
		{
		}
		return this.getResult();
	}

	/**
	 * Can only be called after the algorithm is finished!
	 */
	public GeneratedFramework getResult()
	{
		return null;
	}

	/**
	 * Step the algorithm. Returns true if it has finished
	 */
	public boolean step() throws FailedToGenerateException
	{
		if (this.phase == Phase.CSP)
		{
			//			this.solveCSP();
			this.initialGraph();
//			this.assignConnections();
			this.phase = Phase.FDGD;
			this.gdAlgorithm.initialize(this.fdgdGraph, this.framework.getType(), this.random);
			return false;
		}

		if (this.phase == Phase.FDGD)
		{
			this.gdAlgorithm.step(this.fdgdGraph, this.framework.getType(), this.random, this.fdgdIterations);
			this.fdgdIterations++;

			this.phase = this.gdAlgorithm.inEquilibrium(this.fdgdGraph, this.framework.getType(), this.fdgdIterations);

			if(this.phase == Phase.PATHWAYS && !FDGenUtil.hasCollision(this.fdgdGraph))
			{
				if (this.framework.getType() == FrameworkType.CUBES || !FDGenUtil.hasEdgeIntersections(this.fdgdGraph))
				{
//					//TODO: Might need to be uncommented? Seeing issues
					for (final FDGDNode node : this.fdgdGraph.vertexSet())
						node.assignConnectionsFixRot(this.fdgdGraph.edgesOf(node));

					this.phase = Phase.PATHWAYS;

					this.fragments = new ArrayList<>(this.fdgdGraph.vertexSet().size());
					this.fragments.addAll(this.fdgdGraph.vertexSet());
					this.edgeIterator = this.fdgdGraph.edgeSet().iterator();

					FDGDEdge edge = this.edgeIterator.next();
					PathwayProblem problem = new PathwayProblem(edge.entrance1(), edge.node1(), edge.entrance2(), edge.pathway().pieces(), this.fragments);
					this.pathfindingSolver = new StepAStar<>(problem, heuristicWeight);
					this.pathfindingSolver.step();
					return true;
				}
				else
					this.phase = Phase.REBUILD1;
			}
			return false;
		}

		if (this.phase == Phase.REBUILD1)
		{
			this.assignConnections();
			this.phase = Phase.REBUILD2;
			return false;
		}

		if (this.phase == Phase.REBUILD2)
		{
			this.doSpiderWeb();
//			this.doEdgeDestroy();
			this.gdAlgorithm.resetOnSpiderweb(this.fdgdGraph, this.framework.getType(), this.fdgdIterations);
			this.phase = Phase.REBUILD3;
			return false;
		}

		if (this.phase == Phase.REBUILD3)
		{
			//TODO: Make sure this second connection assignment is helpful at all
			this.assignConnections();
			this.phase = Phase.FDGD;
			return false;
		}

		//Pathways phase

		if (this.pathfindingSolver == null || this.pathfindingSolver.isTerminated())
		{
			if (this.edgeIterator.hasNext())
			{
				for (PathwayNode node : this.pathfindingSolver.currentState().fullPath())
					if (node != null)
						this.fragments.add(node);
				FDGDEdge edge = this.edgeIterator.next();
				PathwayProblem problem = new PathwayProblem(edge.entrance1(), edge.node1(), edge.entrance2(), edge.pathway().pieces(), this.fragments);
				this.pathfindingSolver = new StepAStar<>(problem, heuristicWeight);
			}
			else
				return true;
		}

		this.pathfindingSolver.step();

		return false;
	}

	private void initialGraph() throws FailedToGenerateException
	{
		this.fdgdGraph = new Graph<>();
		final Map<FrameworkNode, FDGDNode> nodeLookup = new HashMap<>(this.framework.graph.vertexSet().size());

		this._nodeMap = nodeLookup; // temperorary;

		//Create all nodes, which are now fixed size
		for (FrameworkNode node : this.framework.graph.vertexSet())
		{
			BlueprintData data = node.possibleValues(this.random).get(0);
			if (data != null)
			{
				//TODO: Proper tolerance dist
				final FDGDNode newNode = new FDGDNode(data, BlockPos.ORIGIN, 0);
				this.fdgdGraph.addVertex(newNode);
				nodeLookup.put(node, newNode);
			}
		}

		//Create the edges between them, which now have a chosen PathwayData for them
		for (FrameworkEdge edge : this.framework.graph.edgeSet())
		{
			PathwayData pathway = null;
			for (PathwayData p1 : edge.node1().pathways())
				for(PathwayData p2 : edge.node2().pathways())
					if (p1 == p2)
						pathway = p1;
			if (pathway != null)
			{
				final FDGDNode node1 = nodeLookup.get(edge.node1());
				final FDGDNode node2 = nodeLookup.get(edge.node2());
				final FDGDEdge newEdge = new FDGDEdge(node1, node2, pathway);
				this.fdgdGraph.addEdge(node1, node2, newEdge);
			}
		}
	}

	//	private void solveCSP() throws FailedToGenerateException
	//	{
	//		//Solve all the conditions in the framework
	//		final FrameworkCSP csp = new FrameworkCSP(this.options.getRandom(), this.framework.conditions, this.framework.graph, this.framework.pathways());
	//		final Map<Object, Object> assignments = CSPSolver.solve(csp);
	//
	//		if (assignments == null)
	//		{
	//			throw new FailedToGenerateException("Couldn't generate framework because it couldn't satisfy its conditions. This is likely an error in how you build your framework up!");
	//		}
	//
	//		//From the solution of the conditions, generate a new graph with the chosen
	//		//objects in place so we can start shaping it
	//		this.fdgdGraph = new SimpleGraph<FDGDNode, FDGDEdge>(FDGDEdge.class);
	//		final Map<FrameworkNode, FDGDNode> nodeLookup = new HashMap<FrameworkNode, FDGDNode>(this.framework.graph.vertexSet().size());
	//
	//		//Create all nodes, which are now fixed size
	//		for (final Entry<Object, Object> entry : assignments.entrySet())
	//		{
	//			final Object key = entry.getKey();
	//			if (key instanceof FrameworkNode)
	//			{
	//				final FrameworkNode itfd = (FrameworkNode) key;
	//				final IConnectable result = (IConnectable) entry.getValue();
	//				if (result != null)
	//				{
	//					final FDGDNode newNode = new FDGDNode(result, itfd.approxPosition());
	//					this.fdgdGraph.addVertex(newNode);
	//					nodeLookup.put(itfd, newNode);
	//				}
	//			}
	//		}
	//
	//		//Create the edges between them, which now have a chosen PathwayData for them
	//		for (final Entry<Object, Object> entry : assignments.entrySet())
	//		{
	//			final Object key = entry.getKey();
	//			if (key instanceof FrameworkEdge)
	//			{
	//				final FrameworkEdge edge = (FrameworkEdge) key;
	//				final PathwayData pathway = (PathwayData) entry.getValue();
	//				if (pathway != null)
	//				{
	//					final FDGDNode node1 = nodeLookup.get(edge.node1());
	//					final FDGDNode node2 = nodeLookup.get(edge.node2());
	//					final FDGDEdge newEdge = new FDGDEdge(node1, node2, pathway);
	//					this.fdgdGraph.addEdge(node1, node2, newEdge);
	//				}
	//			}
	//		}
	//	}

	private void assignConnections()
	{
		for (final FDGDNode node : this.fdgdGraph.vertexSet())
		{
			node.assignConnections(this.fdgdGraph.edgesOf(node));
			//DEBUG
			for (FDGDEdge edge : this.fdgdGraph.edgesOf(node))
			{
				BlockPos min1 = node.getMin();
				BlockPos max1 = node.getMax();

				if (edge.xOf(node) != min1.getX() && edge.zOf(node) != min1.getZ() && edge.xOf(node) != max1.getX() && edge.zOf(node) != max1.getZ())
				{
					//					OrbisCore.debugPrint("Entrance was not placed on an edge.");
				}
				if (edge.xOf(node) < min1.getX() || edge.zOf(node) < min1.getZ() || edge.xOf(node) > max1.getX() || edge.zOf(node) > max1.getZ())
				{
					//					OrbisCore.debugPrint("Entrance was not placed on an edge.");
				}

			}
		}
	}

	private void clearUselessIntersections(List<FDGDEdge> edges)
	{
		for (int p = 0; p < edges.size(); p++)
		{
			FDGDEdge edge = edges.get(p);
			FDGDNode n1 = edge.node1();
			FDGDNode n2 = edge.node2();
			if (n1.isIntersection() || n2.isIntersection())
			{
				this.fdgdGraph.removeEdge(edge);
				FDGDNode start = n1.isIntersection() ? n1 : n2;
				FDGDNode end = edge.getOpposite(start);
				// If we can remove the edge between two intersections and still
				// reach the two intersections by JUST going over other intersections,
				// we can savely remove it.
				if (!this.fdgdGraph.canReach(end, start, FDGDNode::isIntersection))
					this.fdgdGraph.addEdge(n1, n2, edge);
				else
				{
					edges.remove(p);
					p--;
				}
			}
		}

		final List<FDGDNode> nodes = new ArrayList<>(this.fdgdGraph.vertices);
		for (int p = 0; p < nodes.size(); p++)
		{
			FDGDNode n = nodes.get(p);
			Set<FDGDEdge> edgesOut = this.fdgdGraph.edgesOf(n);
			if(n.isIntersection() && edgesOut.size() < 3)
			{
				List<FDGDEdge> edgesOutL = new ArrayList<>(edgesOut);
				if (edgesOut.size() == 2)
				{
					FDGDEdge e1 = edgesOutL.get(0);
					FDGDEdge e2 = edgesOutL.get(1);
					FDGDNode n1 = e1.getOpposite(n);
					FDGDNode n2 = e2.getOpposite(n);
					FDGDEdge eNew = new FDGDEdge(n1, n2, e1.pathway());
					this.fdgdGraph.addEdge(n1, n2, eNew);
				}
				for (int q = 0; q < edgesOutL.size(); q++)
				{
					FDGDEdge e = edgesOutL.get(q);
					this.fdgdGraph.removeEdge(e);
					edges.remove(e);
				}
				this.fdgdGraph.removeVertice(n);
				nodes.remove(p);
				p--;
			}
		}
	}


	private boolean doSpiderWeb()
	{
		boolean sFinished = true;

		final List<FDGDEdge> edges = new ArrayList<>(this.fdgdGraph.edgeSet());
		Collections.shuffle(edges);
		//TODO: Make sure this is always larger than edges.size()
		final int maxAmount = (int) Math.pow(edges.size(), spiderwebGrowth);
		OrbisAPI.LOGGER.info(maxAmount);

//		for (int j = 0; j < nodes.size(); j++)
//		{
//			FDGDNode n = nodes.get(j);
//			if (n.isIntersection())
//			{
//				FDGDEdge e1 = n.getOldEdge1();
//				FDGDEdge e2 = n.getOldEdge2();
//				if (!FDGenUtil.isIntersecting(e1, e2) &&
//						!FDGenUtil.hasEdgeIntersections(this.fdgdGraph, e1) &&
//						!FDGenUtil.hasEdgeIntersections(this.fdgdGraph, e2))
//				{
//
//				}
//			}
//		}

		this.clearUselessIntersections(edges);
		int i = 0;
		outerloop:
		while (i < maxAmount)
		{
			sFinished = true;
			// Not using foreach loops here because we remove the contents of the edges in the loop.
			for (int p = 0; p < edges.size(); p++)
			{
				for (int q = 0; q < edges.size(); q++)
				{
					final FDGDEdge edge1 = edges.get(p);
					final FDGDEdge edge2 = edges.get(q);

					final FDGDNode e1S = edge1.node1();
					final FDGDNode e1T = edge1.node2();

					final FDGDNode e2S = edge2.node1();
					final FDGDNode e2T = edge2.node2();


					// Filter intersections that happen because the edges have the same origin
					if (e1T == e2T || e1T == e2S || e1S == e2T || e1S == e2S)
						continue;
					if (!FDGenUtil.isIntersecting(edge1, edge2))
						continue;

					sFinished = false;
					BlockPos connE1S = edge1.node1().centerAsBP();//.connectionOf1().getPos();
					BlockPos connE1T = edge1.node2().centerAsBP();//connectionOf2().getPos();

					BlockPos connE2S = edge2.node1().centerAsBP();//connectionOf1().getPos();
					BlockPos connE2T = edge2.node2().centerAsBP();//connectionOf2().getPos();

					//Find intersection point of the two lines
					if (connE1S.getX() > connE1T.getX())
					{
						final BlockPos temp = connE1S;
						connE1S = connE1T;
						connE1T = temp;
					}

					if (connE2S.getX() > connE2T.getX())
					{
						final BlockPos temp = connE2S;
						connE2S = connE2T;
						connE2T = temp;
					}

					final long x1 = connE1S.getX();
					final long z1 = connE1S.getZ();

					final long x2 = connE1T.getX();
					final long z2 = connE1T.getZ();

					final long x3 = connE2S.getX();
					final long z3 = connE2S.getZ();

					final long x4 = connE2T.getX();
					final long z4 = connE2T.getZ();

					final long product1 = x1 * z2 - z1 * x2;
					final long product2 = x3 * z4 - z3 * x4;

					final float denominator = (x1 - x2) * (z3 - z4) - (z1 - z2) * (x3 - x4);

					final long nominX = product1 * (x3 - x4) - (x1 - x2) * product2;
					final long nominZ = product1 * (z3 - z4) - (z1 - z2) * product2;

					final float x = nominX / denominator;
					final float y = (connE1S.getY() + connE1T.getY() + connE2S.getY() + connE2T.getY()) / 4f;
					final float z = nominZ / denominator;

					// Find the intersection blueprint
					final BlueprintData intersectionTFD = this.framework.getIntersection(edge1.pathway(), edge2.pathway());

					//Remove old edges
					edges.remove(edge1);
					edges.remove(edge2);

					this.fdgdGraph.removeEdge(edge1);
					this.fdgdGraph.removeEdge(edge2);

					//Add new node and edges, with the intersection blueprint as data.
					//TODO: Proper tolerance dist
					final FDGDNode node = new FDGDNode(intersectionTFD, new BlockPos(x, y, z), edge1, edge2, 0);
					this.fdgdGraph.addVertex(node);

					final FDGDEdge nEdge1 = new FDGDEdge(node, edge1.node1(), edge1.pathway());
					this.fdgdGraph.addEdge(node, edge1.node1(), nEdge1);
					final FDGDEdge nEdge2 = new FDGDEdge(node, edge1.node2(), edge1.pathway());
					this.fdgdGraph.addEdge(node, edge1.node2(), nEdge2);

					final FDGDEdge nEdge3 = new FDGDEdge(node, edge2.node1(), edge2.pathway());
					this.fdgdGraph.addEdge(node, edge2.node1(), nEdge3);
					final FDGDEdge nEdge4 = new FDGDEdge(node, edge2.node2(), edge2.pathway());
					this.fdgdGraph.addEdge(node, edge2.node2(), nEdge4);

					//TODO: Should I add the edges to the open edge list?

					//Reasoning behind this: p will be the lowest of the two. The edge it was currently
					//looking at with index p is now removed so on index p there's the next edge.
					//We want to continue by looking from that edge.
					p--;
					i++;
					if(i >= maxAmount)
						break outerloop;
					break;
				}
			}
			if (sFinished) //There are no longer any intersecting edges left
				break;
		}
		this.clearUselessIntersections(edges);
		return sFinished;
	}

	private void doEdgeDestroy()
	{
		final List<FDGDEdge> edges = new ArrayList<>(this.fdgdGraph.edgeSet());
		//TODO: Make sure this is always larger than edges.size()
		final int maxAmount = (int) Math.pow(edges.size(), graphDestroy);
		OrbisAPI.LOGGER.info(maxAmount);

		int i = 0;
		FDGDEdge removed = null;
		outerloop:
		while (i < maxAmount)
		{
			if(removed != null)
				edges.remove(removed);
			// Not using foreach loops here because we remove the contents of the edges in the loop.
			for (FDGDEdge edge1 : edges)
			{
				for (FDGDEdge edge2 : edges)
				{
					final FDGDNode e1S = edge1.node1();
					final FDGDNode e1T = edge1.node2();

					final FDGDNode e2S = edge2.node1();
					final FDGDNode e2T = edge2.node2();

					// Filter intersections that happen because the edges have the same origin
					if (e1T == e2T || e1T == e2S || e1S == e2T || e1S == e2S)
						continue;

					if (!FDGenUtil.isIntersecting(edge1, edge2))
						continue;

					final FDGDEdge toRemove = this.random.nextBoolean() ? edge1 : edge2;
					//Remove the edge, and make sure we can still reach the nodes it connected
					this.fdgdGraph.removeEdge(toRemove);
					if (this.fdgdGraph.canReach(toRemove.node1(), toRemove.node2()))
					{
						removed = toRemove;
						i++;
						// TODO This is an inefficient way to go over the loop
						continue outerloop;
					}
					// This doesn't happen to be possible. Put it back.
					this.fdgdGraph.addEdge(toRemove.node1(), toRemove.node2(), toRemove);
				}
			}
			// We haven't found any edges we could remove. Let's stop trying.
			break;
		}
		Region bbx = FDGenUtil.boundingBox(this.fdgdGraph);
		float diagonal = FDGenUtil.euclidian(bbx.getMin(), bbx.getMax());
		outerloop:
		for (int j = 0; j < i; j++)
		{
			List<FDGDNode> vertices = new ArrayList<>(this.fdgdGraph.vertices);
			Collections.shuffle(vertices);
			for (FDGDNode n : vertices)
				//TODO: Add amount of entrances constraint here
				for (FDGDNode n2 : vertices)
					if (this.fdgdGraph.edgesOf(n).size() < 3 &&
							this.fdgdGraph.edgesOf(n2).size() < 3 &&
							n != n2 && this.fdgdGraph.getEdge(n, n2) == null)
					{
						float dist = FDGenUtil.euclidian(n.centerAsBP(), n2.centerAsBP());
						if(dist < diagonal * addEdgeDistanceRatio)
						{
							// TODO: Properly choose pathway
							PathwayData p = n.getData().entrances().get(0).toConnectTo();
							FDGDEdge e = new FDGDEdge(n, n2, p);
							// Make sure this added edge does not intersect with any
							// edges already in the graph
							boolean isIntersectingOthers = false;
							for (FDGDEdge e2 : this.fdgdGraph.edgeSet())
								if(FDGenUtil.isIntersecting(e, e2))
								{
									isIntersectingOthers = true;
									break;
								}
							if (!isIntersectingOthers)
							{
								OrbisAPI.LOGGER.info("Adding edge");
								this.fdgdGraph.addEdge(n, n2, e);
								continue outerloop;
							}
						}
					}
		}
	}

	public Graph<FDGDNode, FDGDEdge> getFDGDDebug()
	{
		return this.fdgdGraph;
	}

	public int getIterations()
	{
		return this.fdgdIterations;
	}

	public List<BlueprintRegion> getFragments()
	{
		return this.fragments;
	}

	public Iterable<PathwayNode> getPathfindingDebug()
	{
		return this.pathfindingSolver.currentState().fullPath();
	}

	public Phase getPhase()
	{
		return this.phase;
	}

	public enum Phase
	{
		CSP, FDGD, PATHWAYS, REBUILD1, REBUILD2, REBUILD3
	}
}
