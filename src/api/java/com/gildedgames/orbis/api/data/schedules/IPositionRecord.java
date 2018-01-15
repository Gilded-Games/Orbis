package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.IShape;
import net.minecraft.util.math.BlockPos;

public interface IPositionRecord<DATA> extends IShape, IDimensions
{

	int getVolume();

	void listen(IPositionRecordListener listener);

	boolean unlisten(IPositionRecordListener listener);

	boolean contains(int index);

	DATA[] getData();

	DATA get(int index);

	DATA get(int x, int y, int z);

	void markPos(DATA data, int x, int y, int z);

	void unmarkPos(int x, int y, int z);

	Iterable<BlockPos.MutableBlockPos> getPositions(DATA data);

}