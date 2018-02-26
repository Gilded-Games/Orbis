package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * <p> A Framework is a <strong>connected graph-based data structure</strong> that can be generated using
 * Orbis's Framework algorithm. This can be used to model large
 * collections of structures, such as cities or dungeons.
 *
 * <p> The graph is build up off of <strong>nodes and edges</strong> connecting the nodes. 
 * Edges represent that a pathway will be generated between the buildings 
 * represented by the nodes. The nodes then are 
 * saved in the {@link FrameworkNode FrameworkNode} class. First of
 * all, they have some sort of data inside of them. Right now, this can
 * be a <tt>ScheduleData</tt> or another <tt>FrameworkData</tt>.
 *
 * <p> This class contains Conditions on how the various nodes are going to
 * turn out. This can be used to model relations such as that each node
 * needs to choose a different <tt>BlueprintData</tt>, or that some building
 * should only generate once.
 *
 * <p> There are two {@link FrameworkType types} of Frameworks, a 2D one
 * called rectangles and a 3D one called cubes.  
 *
 * <p> When the FrameworkData generates, it's possible that there are <strong>intersections</strong>
 * between two edges in the 2D case. When this happens, the algorithm adds a new node, 
 * called an intersection. What Schedule is behind this intersection 
 * also needs to be chosen.
 *
 * <p> <tt>FrameworkData</tt> also contains a lot of parameters. They are
 * created with the <tt>paramFac</tt> as an <tt>IFrameworkParams</tt>. 
 * It changes strongly how the <tt>FrameworkAlgorithm</tt> is going to run.
 *
 * <p> It is very important that at all times the graph behind the Framework 
 * is <strong>connected</strong>. This means that there is a path over the
 * Edges between each node
 *
 * @author Emile
 *
 * @see FrameworkAlgorithm
 * @see FrameworkNode
 * @see FrameworkType
 * @see PathwayData
 *
 */
public class FrameworkData implements IFrameworkNode, IData, IDimensions
{

	private final static Object stub = "aweoigh";

	/**
	 * The underlying graph of a Framework. It is an undirected graph with at most
	 * one edge between its nodes.  
	 */
	protected final Graph<FrameworkNode, FrameworkEdge> graph = new Graph<>();

	/**
	 * The list of all conditions on the nodes.
	 */
	//	protected final List<Condition<Object>> conditions = new ArrayList<Condition<Object>>();

	private final FrameworkType type = FrameworkType.RECTANGLES;

	/**
	 * A map that contains what blueprint to use when two pathways intersect. This is only necessary
	 * when {@link #type the FrameworkType} is {@link FrameworkType#RECTANGLES Rectangles}.
	 */
	private final Map<Tuple<PathwayData, PathwayData>, BlueprintData> intersections = new HashMap<>();

	private final Map<IFrameworkNode, BlockPos> nodeToPos = Maps.newHashMap();

	private final List<IFrameworkDataListener> listeners = Lists.newArrayList();

	private int width, height, length;

	public FrameworkData(int width, int height, int length)
	{
		this.width = width;
		this.height = height;
		this.length = length;
	}

	public void listen(IFrameworkDataListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	public boolean unlisten(IFrameworkDataListener listener)
	{
		return this.listeners.remove(listener);
	}

	public Map<IFrameworkNode, BlockPos> getNodeToPosMap()
	{
		return this.nodeToPos;
	}

	/**
	 * Executes the Framework algorithm and returns a list with blueprints and positions
	 * in a <tt>GeneratedFramework</tt>.
	 *
	 * @see FrameworkAlgorithm
	 *
	 * @param world The world we want to generate this Framework in. Used for checking conditions
	 * and as the height map.
	 * @param pos Not sure yet :/
	 * @return The list with chosen blueprints and positions for them.
	 */
	public GeneratedFramework prepare(World world, BlockPos pos)
	{
		//TODO: What really does the pos here represent? We need the pos to make sure the Framework
		//shapes well around the terrain, but where can it actually generate at all?

		final FrameworkAlgorithm algorithm = new FrameworkAlgorithm(this, world);
		return algorithm.computeFully();
	}

	public FrameworkEdge edgeBetween(FrameworkNode node1, FrameworkNode node2)
	{
		return this.graph.getEdge(node1, node2);
	}

	public BlockPos getRelativePos(IFrameworkNode node)
	{
		return this.nodeToPos.get(node);
	}

	/**
	 * <p>Adds a node to the Framework. Throws an <tt>IllegalStateException</tt>
	 * when there is already a node on the given position. Nodes should
	 * always be added to the Framework before the edges.
	 *
	 * <p>Note that adding a node destroys the connectivity property of the graph,
	 * unless it is the very first one. After the node is added,
	 *
	 * @param data The data inside of this node. Right now, this can be
	 * @param initialRelativePos The initial relative position that the node is located
	 *                           at within the Framework
	 * @return The created FrameworkNode
	 */
	public FrameworkNode addNode(IFrameworkNode data, BlockPos initialRelativePos)
	{
		if (this.nodeToPos.values().contains(initialRelativePos))
		{
			throw new IllegalStateException("Another node is already at this relative position. Offending pars: " + data + ", " + initialRelativePos);
		}

		final FrameworkNode newNode = new FrameworkNode(data);
		this.graph.addVertex(newNode);

		this.nodeToPos.put(data, initialRelativePos);

		this.listeners.forEach(l -> l.onAddNode(data, initialRelativePos));

		return newNode;
	}

	/**
	 * Adds an edge between two nodes. When the two nodes given are not
	 * yet added to the framework, this returns false. Furthermore, if no
	 * more edges are allowed for one of the two nodes, it does the same.
	 * @return True if the edge was successfully added
	 */
	public boolean addEdge(FrameworkNode node1, FrameworkNode node2)
	{
		if (!this.graph.containsVertex(node1) || !this.graph.containsVertex(node2))
		{
			return false;
		}
		if (this.graph.edgesOf(node1).size() >= node1.schedule().maxEdges() || this.graph.edgesOf(node2).size() >= node2.schedule().maxEdges())
		{
			return false;
		}
		final FrameworkEdge edge = new FrameworkEdge(node1, node2);
		this.graph.addEdge(node1, node2, edge);

		this.listeners.forEach(l -> l.onAddEdge(node1, node2));
		return true;
	}

	public FrameworkEdge edgeAt(FrameworkNode n1, FrameworkNode n2)
	{
		return this.graph.getEdge(n1, n2);
	}

	/**
	 * Adds an intersection blueprint for when the two given Pathways
	 * intersect. The blueprint needs at least 4 Entrances, otherwise
	 * this will throw an IllegalArgumentException.
	 */
	public void addIntersection(PathwayData pathway1, PathwayData pathway2, BlueprintData blueprint)
	{
		if (blueprint.entrances().size() < 4)
		{
			throw new IllegalArgumentException("Can only have intersection blueprints with 4 or more entrances");
		}
		this.intersections.put(new Tuple<>(pathway1, pathway2), blueprint);

		this.listeners.forEach(l -> l.onAddIntersection(pathway1, pathway2, blueprint));
	}

	public FrameworkType getType()
	{
		return this.type;
	}

	@Override
	public int maxEdges()
	{
		// TODO Auto-generated method stub
		return 100;
	}

	public BlueprintData getIntersection(PathwayData pathway1, PathwayData pathway2)
	{
		for (Tuple<PathwayData, PathwayData> t : this.intersections.keySet())
		{
			if (t.getFirst() == pathway1 && t.getSecond() == pathway2 ||
					t.getFirst() == pathway2 && t.getSecond() == pathway1)
			{
				return this.intersections.get(t);
			}
		}
		return null;
	}

	@Override
	public IDimensions largestPossibleDim()
	{
		return this;
	}

	@Override
	public List<BlueprintData> possibleValues(Random random)
	{
		//TODO
		return new ArrayList<>();
	}

	@Override
	public Collection<PathwayData> pathways()
	{
		final Set<PathwayData> schedules = new HashSet<>();
		for (final FrameworkNode node : this.graph.vertexSet())
		{
			schedules.addAll(node.pathways());
		}
		return schedules;
	}

	@Override
	public void preSaveToDisk(IWorldObject object)
	{

	}

	@Override
	public IData clone()
	{
		return null;
	}

	@Override
	public String getFileExtension()
	{
		return null;
	}

	@Override
	public IDataMetadata getMetadata()
	{
		return null;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("type", this.type.ordinal());

	}

	@Override
	public void read(NBTTagCompound tag)
	{

	}

	public Graph<FrameworkNode, FrameworkEdge> getGraph()
	{
		return this.graph;
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
}
