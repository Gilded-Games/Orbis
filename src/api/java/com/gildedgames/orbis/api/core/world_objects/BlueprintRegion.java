package com.gildedgames.orbis.api.core.world_objects;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.region.AbstractRegion;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IRotateable;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlueprintRegion extends AbstractRegion implements IMutableRegion, IRotateable
{
	protected Rotation rotation = Rotation.NONE;

	protected BlueprintData data;

	protected BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	protected BlueprintRegion()
	{

	}

	protected BlueprintRegion(final World world)
	{
	}

	public BlueprintRegion(final IRegion region)
	{
		this.data = new BlueprintData(region);

		this.setBounds(region);
	}

	public BlueprintRegion(final BlockPos pos, final BlueprintData data)
	{
		this.data = data;

		this.setPos(pos);
	}

	public BlueprintRegion(final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		this.data = data;
		this.rotation = rotation;

		this.setPos(pos);
	}

	@Override
	public Rotation getRotation()
	{
		return this.rotation;
	}

	public BlockData getBlock(final BlockPos pos)
	{
		final BlockPos transformed = this.transformForBlueprint(pos);
		return this.getBlockDataContainer().get(transformed);
	}

	public BlockPos transformForBlueprint(final BlockPos pos)
	{
		final Rotation transformRot =
				this.rotation == Rotation.CLOCKWISE_90 ?
						Rotation.COUNTERCLOCKWISE_90 :
						this.rotation == Rotation.COUNTERCLOCKWISE_90 ? Rotation.CLOCKWISE_90 : this.rotation;
		final BlockPos rotated = RotationHelp.rotate(pos, this, transformRot);
		final IRegion rotatedRegion = RotationHelp.rotate(this, transformRot);
		return new BlockPos(rotated.getX() - rotatedRegion.getMin().getX(), rotated.getY() - rotatedRegion.getMin().getY(),
				rotated.getZ() - rotatedRegion.getMin().getZ());
	}

	@Override
	public int getWidth()
	{
		return this.data.getWidth();
	}

	@Override
	public int getHeight()
	{
		return this.data.getHeight();
	}

	@Override
	public int getLength()
	{
		return this.data.getLength();
	}

	public BlockPos getPos()
	{
		return this.min;
	}

	public void setPos(final BlockPos pos)
	{
		this.min = pos;
		int width = this.rotation == Rotation.NONE || this.rotation == Rotation.CLOCKWISE_180 ? this.getWidth() : this.getLength();
		int length = this.rotation == Rotation.NONE || this.rotation == Rotation.CLOCKWISE_180 ? this.getLength() : this.getWidth();
		this.max = RegionHelp.getMax(this.min, width, this.getHeight(), length);

		this.notifyDataChange();
	}

	@Override
	public BlockPos getMin()
	{
		return this.min;
	}

	@Override
	public BlockPos getMax()
	{
		return this.max;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("min", this.min);

		tag.setString("rotation", this.rotation.name());

		funnel.set("state", this.data);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.min = funnel.getPos("min");

		this.rotation = Rotation.valueOf(tag.getString("rotation"));

		this.data = funnel.get("state");

		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.notifyDataChange();
	}

	@Override
	public void setBounds(final IRegion region)
	{
		this.min = region.getMin();
		this.max = region.getMax();

		this.notifyDataChange();
	}

	@Override
	public void setBounds(final BlockPos corner1, final BlockPos corner2)
	{
		this.min = RegionHelp.getMin(corner1, corner2);
		this.max = RegionHelp.getMax(corner1, corner2);

		this.notifyDataChange();
	}

	public BlockDataContainer getBlockDataContainer()
	{
		return this.data.getBlockDataContainer();
	}

	public BlueprintData getData()
	{
		return this.data;
	}

}
