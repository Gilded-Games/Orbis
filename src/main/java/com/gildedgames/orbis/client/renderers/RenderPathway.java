package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderPathway implements IWorldRenderer
{
	private final Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private boolean disabled;

	private Collection<BlueprintData> pieces;

	private PathwayNode startingNode;

	private Map<BlueprintData, RenderBlueprintBlocks> pieceToRenderCache = Maps.newHashMap();

	private IRegion pathwayRegion;

	public RenderPathway(final Collection<BlueprintData> pieces, PathwayNode startingNode, IRegion pathwayRegion)
	{
		this.pieces = pieces;
		this.startingNode = startingNode;
		this.pathwayRegion = pathwayRegion;
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

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.startingNode;
	}

	public void cachePieces(World world, float partialTicks, boolean useCamera)
	{
		for (BlueprintData piece : this.pieces)
		{
			Blueprint bp = new Blueprint(world, BlockPos.ORIGIN, piece);

			RenderBlueprintBlocks render = new RenderBlueprintBlocks(bp, world);

			render.shapeData = bp.createShapeData();

			render.cacheRender(world, partialTicks, useCamera);

			this.pieceToRenderCache.put(piece, render);
		}
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.pathwayRegion;
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
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		if (this.pieceToRenderCache.isEmpty())
		{
			this.cachePieces(world, partialTicks, useCamera);
		}

		if (this.startingNode == null)
		{
			return;
		}

		GlStateManager.pushMatrix();

		final double offsetPlayerX = this.mc.player.lastTickPosX + (this.mc.player.posX - this.mc.player.lastTickPosX) * partialTicks;
		final double offsetPlayerY = this.mc.player.lastTickPosY + (this.mc.player.posY - this.mc.player.lastTickPosY) * partialTicks;
		final double offsetPlayerZ = this.mc.player.lastTickPosZ + (this.mc.player.posZ - this.mc.player.lastTickPosZ) * partialTicks;

		if (useCamera)
		{
			GlStateManager.translate(-offsetPlayerX, -offsetPlayerY, -offsetPlayerZ);
		}

		for (PathwayNode n : this.startingNode.fullPath())
		{
			GlStateManager.pushMatrix();

			RenderBlueprintBlocks render = this.pieceToRenderCache.get(n.getData());

			float angle = 0.0F;

			switch (n.getRotation())
			{
				case NONE:
					angle = 0.0F;
					break;
				case CLOCKWISE_90:
					angle = 270.0F;
					break;
				case CLOCKWISE_180:
					angle = 180.0F;
					break;
				case COUNTERCLOCKWISE_90:
					angle = 90.0F;
					break;
			}

			GlStateManager.translate(n.getPos().getX(), n.getPos().getY(), n.getPos().getZ());

			float oldMaxX = n.getMin().getX() + n.getBlockDataContainer().getWidth();
			float oldMaxZ = n.getMin().getZ() + n.getBlockDataContainer().getLength();

			float xDif = (n.getMax().getX() - oldMaxX) / 2.0F;
			float zDif = Math.abs(n.getMax().getZ() - oldMaxZ) / 2.0F;

			GlStateManager.translate(xDif, 0, zDif);

			GlStateManager.translate(n.getWidth() / 2.0F, n.getHeight() / 2.0F, n.getLength() / 2.0F);

			GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);

			GlStateManager.translate(-n.getWidth() / 2.0F, -n.getHeight() / 2.0F, -n.getLength() / 2.0F);

			/*if (n.getRotation() == Rotation.CLOCKWISE_90)
			{
				GlStateManager.translate(1.0F, 0, 0.0F);
			}

			if (n.getRotation() == Rotation.COUNTERCLOCKWISE_90)
			{
				GlStateManager.translate(-1.0F, 0, -0.0F);
			}*/

			if (n.getRotation() == Rotation.CLOCKWISE_90)
			{

			}
			else
			{
				int roundX = RotationHelp.getRounding(n.getWidth());
				int roundZ = RotationHelp.getRounding(n.getLength());
				//GlStateManager.translate(-roundX, 2, -roundZ);
			}

			RenderHelper.disableStandardItemLighting();

			if (Minecraft.isAmbientOcclusionEnabled())
			{
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
			}
			else
			{
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}

			GlStateManager.callList(render.getGlIndex());

			RenderHelper.enableStandardItemLighting();

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.resetColor();

			GlStateManager.popMatrix();
		}

		if (useCamera)
		{
			GlStateManager.translate(0, 0, 0);
		}

		GlStateManager.resetColor();

		GlStateManager.popMatrix();
	}

	@Override
	public void preRenderSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void onRemoved()
	{
		for (RenderBlueprintBlocks render : this.pieceToRenderCache.values())
		{
			GlStateManager.glDeleteLists(render.getGlIndex(), 1);
		}
	}
}
