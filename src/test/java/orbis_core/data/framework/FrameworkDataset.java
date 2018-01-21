package orbis_core.data.framework;

import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.math.BlockPos;
import orbis_core.data.BlueprintDataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrameworkDataset
{
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

	public static FrameworkData randomFramework(Random random)
	{
		FrameworkData frameworkData = new FrameworkData();
		int amt_nodes = random.nextInt(140) + 1;
		List<FrameworkNode> nodes  = new ArrayList<>();
		for(int i = 0; i < amt_nodes; i++)
			nodes.add(frameworkData.addNode(BlueprintDataset.randomSchedule(random)));
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
		float probability = 0.3f / amt_nodes;
		for (FrameworkNode n1 : nodes)
		{
			for (FrameworkNode n2 : nodes)
			{
				if(n1 != n2 && frameworkData.edgeAt(n1, n2) == null)
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
