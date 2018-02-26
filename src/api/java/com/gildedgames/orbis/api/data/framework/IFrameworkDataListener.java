package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import net.minecraft.util.math.BlockPos;

public interface IFrameworkDataListener
{

	void onAddNode(IFrameworkNode node, BlockPos initialRelativePos);

	void onAddEdge(FrameworkNode n1, FrameworkNode n2);

	void onAddIntersection(PathwayData pathway1, PathwayData pathway2, BlueprintData blueprint);

}
