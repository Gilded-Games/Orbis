package orbis_core.data.framework;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.math.BlockPos;
import orbis_core.data.BlueprintDataset;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrameworkDataset
{
	private static BlueprintData pathwayB = null;

	public static BlueprintData getPathwayB(PathwayData pathway)
	{
		if (pathwayB == null)
		{
			BlueprintData b1 = new BlueprintData(new BlockDataContainer(5, 5, 5));
			b1.addEntrance(new Entrance(new BlockPos(0, 0, 2), pathway));
			b1.addEntrance(new Entrance(new BlockPos(4, 0, 2), pathway));
			b1.addEntrance(new Entrance(new BlockPos(2, 0, 0), pathway));
			b1.addEntrance(new Entrance(new BlockPos(2, 0, 4), pathway));
			pathwayB = b1;
		}
		return pathwayB;
	}

	public static FrameworkData framework1()
	{
		final FrameworkData frameworkData = new FrameworkData();
		final FrameworkNode node1 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node2 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node3 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node4 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node5 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node6 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node7 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node8 = frameworkData.addNode(BlueprintDataset.schedule1());
		final FrameworkNode node9 = frameworkData.addNode(BlueprintDataset.schedule1());

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
		FrameworkData frameworkData = new FrameworkData();
		PathwayData pathway = pathway(random);
		frameworkData.addIntersection(pathway, pathway, getPathwayB(pathway));

		int amt_nodes = random.nextInt(140) + 1;
		List<FrameworkNode> nodes  = new ArrayList<>();
		for(int i = 0; i < amt_nodes; i++)
			nodes.add(frameworkData.addNode(BlueprintDataset.randomSchedule(random, pathway)));
		List<FrameworkNode> connectedNodes = new ArrayList<>();
		for (FrameworkNode n1 : nodes)
		{
			if (connectedNodes.size() == 0)
				connectedNodes.add(n1);
			else
			{
				FrameworkNode n2 = connectedNodes.get(random.nextInt(connectedNodes.size()));
				frameworkData.addEdge(n1, n2);
				connectedNodes.add(n1);
			}
		}
		float probability = 0.5f / amt_nodes;
		for (FrameworkNode n1 : nodes)
			for (FrameworkNode n2 : nodes)
				if(n1 != n2 && frameworkData.edgeAt(n1, n2) == null)
					if (random.nextFloat() < probability)
						frameworkData.addEdge(n1, n2);
		return frameworkData;
	}

}
