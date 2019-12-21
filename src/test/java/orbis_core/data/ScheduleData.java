package orbis_core.data;

import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.framework.FrameworkData;
import com.gildedgames.orbis.lib.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.lib.data.pathway.PathwayData;
import com.gildedgames.orbis.lib.data.region.IMutableRegion;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScheduleData implements IFrameworkNode
{
	List<BlueprintData> blueprints;

	public ScheduleData(List<BlueprintData> blueprints)
	{
		this.blueprints = blueprints;
	}

	@Override
	public BlueprintData getBlueprintData()
	{
		return null;
	}

	@Override
	//TODO: This might not be correct.
	public int getMaxEdges()
	{
		return 0;
		//		return this.blueprints.stream()
		//				.mapToInt(b -> b.getEntrance().size())
		//				.max().getAsInt(); TODO: Entrances
	}

	@Override
	public IMutableRegion getBounds()
	{
		return null;
	}

	/*@Override
	public List<BlueprintData> possibleValues(Random random)
	{
		Collections.shuffle(this.blueprints, random);
		return new ArrayList<>(this.blueprints);
	}*/

	@Override
	public Collection<PathwayData> pathways()
	{
		List<PathwayData> pathways = new ArrayList<>();
		for (BlueprintData b : this.blueprints)
		{
			//			for (IEntrance e : b.getEntrance()) TODO: Entrance
			//			{
			//				pathways.add(e.toConnectTo());
			//			}
		}
		return pathways;
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

	@Override
	public Class<? extends FrameworkData> getDataClass()
	{
		return null;
	}

	@Override
	public FrameworkData getDataParent()
	{
		return null;
	}

	@Override
	public void setDataParent(FrameworkData frameworkData)
	{

	}
}
