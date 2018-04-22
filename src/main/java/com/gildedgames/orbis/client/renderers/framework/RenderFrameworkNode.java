package com.gildedgames.orbis.client.renderers.framework;

import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderFrameworkNode implements IWorldRenderer
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final IFrameworkNode node;

	private final Framework framework;

	private boolean disabled = false;

	private RenderShape renderShape;

	private Region boundingBox;

	public RenderFrameworkNode(Framework framework, final IFrameworkNode node)
	{
		this.framework = framework;
		this.node = node;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.boundingBox = new Region(this.node.getBounds());

			this.boundingBox.add(framework.getPos());

			this.renderShape = new RenderShape(this.boundingBox);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = 0xFFFFFF;
			this.renderShape.colorBorder = 0xFFFFFF;
			this.renderShape.boxAlpha = 0.1F;

			this.subRenderers.add(this.renderShape);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabled;
	}

	@Override
	public void setDisabled(final boolean disabled)
	{
		this.disabled = disabled;
	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.node;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.boundingBox;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void preRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public List<IWorldRenderer> getSubRenderers(final World world)
	{
		return this.subRenderers;
	}

	@Override
	public ReadWriteLock getSubRenderersLock()
	{
		return this.lock;
	}

	@Override
	public void onRemoved()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

}
