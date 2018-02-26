package orbis_core.data;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayUtil;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.Region;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlueprintDataset
{
	private static BlueprintData blueprint1;

	private static BlueprintData blueprint2;

	private static BlueprintData blueprint3;

	public static ScheduleData schedule1()
	{
		List<BlueprintData> b = new ArrayList<>();
		b.add(blueprint1());
		b.add(blueprint2());
		b.add(blueprint3());
		return new ScheduleData(b);
	}

	public static ScheduleData randomSchedule(Random random, PathwayData pathway)
	{
		List<BlueprintData> b = new ArrayList<>();
		int min = 5, max = 300;
		int rEntrances = 5;
		if (random.nextFloat() < 0.01)
		{
			max = 1000;
		}
		final BlockDataContainer container = new BlockDataContainer(random.nextInt(max) + min, 1, random.nextInt(max) + min);
		BlueprintData blueprint = new BlueprintData(container);
		b.add(blueprint);
		int amtE = random.nextInt(rEntrances) + 2;
		for (int i = 0; i < amtE; i++)
		{
			int x, z, y = 0;
			if (random.nextBoolean())
			{
				x = random.nextBoolean() ? 0 : blueprint.getWidth() - 1;
				z = random.nextInt(blueprint.getLength()) - 1;

				if (z == 0)
				{
					z = 1;
				}
			}
			else
			{
				x = random.nextInt(blueprint.getWidth()) - 1;
				z = random.nextBoolean() ? 0 : blueprint.getLength() - 1;

				if (x == 0)
				{
					x = 1;
				}
			}

			Region r = new Region(new BlockPos(x, y, z), new BlockPos(x, y + 1, z));
			Region br = new Region(new BlockPos(0, 0, 0), new BlockPos(blueprint.getWidth() - 1, blueprint.getHeight() - 1, blueprint.getLength() - 1));

			Entrance e = new Entrance(r, pathway, PathwayUtil.sidesOfConnection(br, r));
			blueprint.addEntrance(e);
		}
		return new ScheduleData(b);
	}

	public static BlueprintData blueprint1()
	{
		if (blueprint1 != null)
		{
			return blueprint1;
		}
		final BlockDataContainer container = new BlockDataContainer(5, 2, 5);
		blueprint1 = new BlueprintData(container);

		return blueprint1;
	}

	public static BlueprintData blueprint2()
	{
		if (blueprint2 != null)
		{
			return blueprint2;
		}
		final BlockDataContainer container = new BlockDataContainer(4, 5, 12);
		blueprint2 = new BlueprintData(container);

		return blueprint2;
	}

	public static BlueprintData blueprint3()
	{
		if (blueprint3 != null)
		{
			return blueprint3;
		}
		final BlockDataContainer container = new BlockDataContainer(4, 5, 10);
		blueprint3 = new BlueprintData(container);

		//		blueprint3.addEntrance(new BlockPos(2, 1, 0), PathwayDataset.pathway1());
		//		blueprint3.addEntrance(new BlockPos(2, 1, 9), PathwayDataset.pathway1());
		//		blueprint3.addEntrance(new BlockPos(3, 1, 4), PathwayDataset.pathway1());
		//		blueprint3.addEntrance(new BlockPos(3, 1, 8), PathwayDataset.pathway1());

		return blueprint3;
	}
}
