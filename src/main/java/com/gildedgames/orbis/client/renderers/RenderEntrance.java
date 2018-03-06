package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderEntrance implements IWorldRenderer
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Entrance entrance;

	private boolean disabled = false;

	private RenderShape renderShape;

	private Region bb;

	public RenderEntrance(IWorldObject parentObject, final Entrance entrance)
	{
		this.entrance = entrance;

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.bb = new Region(this.entrance.getBounds());
			this.bb.add(parentObject.getPos());

			this.renderShape = new RenderShape(this.bb);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.entrance.getColor();
			this.renderShape.colorBorder = this.entrance.getColor();

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
		return this.entrance;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.bb;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedEntrance() == this.entrance && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getEntrancePower())
		{
			boolean refresh = this.renderShape.boxAlpha == 0.25F;

			this.renderShape.boxAlpha = 0.5F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}
		else
		{
			boolean refresh = this.renderShape.boxAlpha == 0.5F;

			this.renderShape.boxAlpha = 0.25F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}
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
