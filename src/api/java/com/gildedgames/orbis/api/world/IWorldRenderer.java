package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public interface IWorldRenderer extends NBT
{

	/**
	 * Should not render this object if it's disabled.
	 * @return Whether or not this world renderer is
	 * disabled.
	 */
	boolean isDisabled();

	/**
	 * Should not render this object if it's disabled.
	 * @param disabled Whether or not this world renderer is
	 * disabled.
	 */
	void setDisabled(boolean disabled);

	@Nullable
	Object getRenderedObject();

	IRegion getBoundingBox();

	void render(World world, float partialTicks, boolean useCamera);

	void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera);

	void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera);

	List<IWorldRenderer> getSubRenderers(World world);

	ReadWriteLock getSubRenderersLock();

	void onRemoved();

}
