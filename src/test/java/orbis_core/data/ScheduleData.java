package orbis_core.data;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.world.IWorldObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class ScheduleData implements IFrameworkNode
{
	List<BlueprintData> blueprints;

	public ScheduleData(List<BlueprintData> blueprints)
	{
		this.blueprints = blueprints;
	}

	@Override
	//TODO: This might not be correct.
	public int maxEdges()
	{
		return this.blueprints.stream()
				.mapToInt(b -> b.entrances().size())
				.max().getAsInt();
	}

	@Override
	public IMutableRegion getBounds()
	{
		return null;
	}

	@Override
	public List<BlueprintData> possibleValues(Random random)
	{
		Collections.shuffle(this.blueprints, random);
		return new ArrayList<>(this.blueprints);
	}

	@Override
	public Collection<PathwayData> pathways()
	{
		List<PathwayData> pathways = new ArrayList<>();
		for (BlueprintData b : this.blueprints)
		{
			for (Entrance e : b.entrances())
			{
				pathways.add(e.toConnectTo());
			}
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
	public IWorldObject getWorldObjectParent()
	{
		return null;
	}

	@Override
	public void setWorldObjectParent(IWorldObject worldObject)
	{

	}
}
