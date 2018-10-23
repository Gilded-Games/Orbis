package com.gildedgames.orbis.client.model;

import com.gildedgames.orbis.client.OrbisDeveloperEventsClient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ModelOrbisFloor implements IBakedModel
{

	public static IBakedModel currentMimicModel;

	public static IBlockState currentMimicBlock;

	public static boolean useBlock = false;

	public static int color;

	public static void setFloorColor(int color)
	{ //Only works when no mimic is selected
		if (ModelOrbisFloor.color != color)
		{
			ModelOrbisFloor.color = color;
			Minecraft.getMinecraft().renderGlobal.loadRenderers(); // Trigger a world refresh so the models update
		}
	}

	@Nonnull
	public static IBakedModel getMimicModel()
	{
		if (currentMimicModel == null && currentMimicBlock != null)
		{
			currentMimicModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(currentMimicBlock);
		}
		return currentMimicModel != null ? currentMimicModel : OrbisDeveloperEventsClient.original;
	}

	public static void setMimicModel(IBlockState state)
	{
		if (currentMimicBlock != state)
		{
			currentMimicBlock = state;
			currentMimicModel = null;
			Minecraft.getMinecraft().renderGlobal.loadRenderers(); // Trigger a world refresh so the models update
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		return getMimicModel().getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return getMimicModel().isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return getMimicModel().isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return getMimicModel().isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return getMimicModel().getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return getMimicModel().getOverrides();
	}
}