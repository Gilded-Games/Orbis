package orbis_core.data.framework;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.FrameworkEdge;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import com.gildedgames.orbis.api.data.framework.Graph;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayUtil;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.Region;
import net.minecraft.util.math.BlockPos;
import orbis_core.data.BlueprintDataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrameworkDataset
{
	private static BlueprintData pathwayB = null;

	public static BlueprintData getPathwayB(PathwayData pathway)
	{
		if (pathwayB == null || pathwayB.entrances().get(0).toConnectTo() != pathway)
		{
			BlueprintData b1 = new BlueprintData(new BlockDataContainer(10, 2, 15));

			addEntrance(b1, pathway, new Region(new BlockPos(0, 0, 5), new BlockPos(0, 1, 10)));
			addEntrance(b1, pathway, new Region(new BlockPos(9, 0, 5), new BlockPos(9, 1, 10)));
			addEntrance(b1, pathway, new Region(new BlockPos(2, 0, 0), new BlockPos(7, 1, 0)));
			addEntrance(b1, pathway, new Region(new BlockPos(2, 0, 14), new BlockPos(7, 1, 14)));

			pathwayB = b1;
		}
		return pathwayB;
	}

	private static void addEntrance(BlueprintData b, PathwayData pathway, Region region)
	{
		Region br = new Region(new BlockPos(0, 0, 0), new BlockPos(b.getWidth() - 1, b.getHeight() - 1, b.getLength() - 1));

		b.addEntrance(new Entrance(region, pathway, PathwayUtil.sidesOfConnection(br, region)));
	}

	public static FrameworkData framework1()
	{
		final FrameworkData frameworkData = new FrameworkData(200, 200, 200);
		final FrameworkNode node1 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node2 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node3 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node4 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node5 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node6 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node7 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node8 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();
		final FrameworkNode node9 = frameworkData.addNode(BlueprintDataset.schedule1()).getValue();

		frameworkData.addEdge(node1, node2);
		frameworkData.addEdge(node1, node3);
		frameworkData.addEdge(node4, node2);
		frameworkData.addEdge(node3, node4);
		frameworkData.addEdge(node5, node3);
		frameworkData.addEdge(node5, node6);
		frameworkData.addEdge(node4, node6);
		frameworkData.addEdge(node6, node7);
		frameworkData.addEdge(node4, node7);
		frameworkData.addEdge(node7, node9);
		frameworkData.addEdge(node7, node8);
		frameworkData.addEdge(node9, node8);

		//frameworkData.addIntersection(PathwayDataset.pathway1(), PathwayDataset.pathway1(), BlueprintDataset.blueprint1());

		return frameworkData;
	}

	private static PathwayData pathway(Random random)
	{
		PathwayData pathway = new PathwayData();

		pathway.addPiece(getPathwayB(pathway));
		return pathway;
	}

	public static FrameworkData randomFramework(Random random)
	{
		FrameworkData frameworkData = new FrameworkData(300, 300, 300);
		PathwayData pathway = pathway(random);
		frameworkData.addIntersection(pathway, pathway, getPathwayB(pathway));

		int amt_nodes = random.nextInt(140) + 1;
		List<FrameworkNode> nodes = new ArrayList<>();
		for (int i = 0; i < amt_nodes; i++)
		{
			nodes.add(frameworkData.addNode(BlueprintDataset.randomSchedule(random, pathway)).getValue());
		}
		List<FrameworkNode> connectedNodes = new ArrayList<>();
		Graph<FrameworkNode, FrameworkEdge> graph = frameworkData.getGraph();
		for (FrameworkNode n1 : nodes)
		{
			if (connectedNodes.size() == 0)
			{
				connectedNodes.add(n1);
			}
			else
			{
				while (true)
				{
					FrameworkNode n2 = connectedNodes.get(random.nextInt(connectedNodes.size()));
					if (graph.edgesOf(n2).size() < n2.maxEdges())
					{
						frameworkData.addEdge(n1, n2);
						connectedNodes.add(n1);
						break;
					}
				}
			}
		}
		float probability = 0.5f / amt_nodes;
		for (FrameworkNode n1 : nodes)
		{
			for (FrameworkNode n2 : nodes)
			{
				if (n1 != n2
						&& frameworkData.edgeAt(n1, n2) == null
						&& graph.edgesOf(n1).size() < n1.maxEdges()
						&& graph.edgesOf(n2).size() < n2.maxEdges())
				{
					if (random.nextFloat() < probability)
					{
						frameworkData.addEdge(n1, n2);
					}
				}
			}
		}
		return frameworkData;
	}

}
