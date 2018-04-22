package com.gildedgames.orbis.client.renderers.tiles;

import com.gildedgames.orbis_api.common.util.OpenGLHelper;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis.client.OrbisClientCaches;
import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis.client.renderers.RenderUtil;
import com.gildedgames.orbis.client.renderers.framework.RenderFrameworkEditing;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.items.ItemFramework;
import com.gildedgames.orbis.common.tiles.TileEntityFramework;
import com.gildedgames.orbis.common.util.WorldRenderHelp;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TileEntityFrameworkRenderer extends TileEntitySpecialRenderer<TileEntityFramework>
{

	private static final Minecraft mc = Minecraft.getMinecraft();

	public final BakedModel baked = new BakedModel();

	private ItemStack stack;

	public TileEntityFrameworkRenderer()
	{

	}

	@Override
	public void render(final TileEntityFramework te, final double x, final double y, final double z, final float partialTicks,
			final int destroyStage, float alpha)
	{
		try
		{
			if (this.stack == null)
			{
				return;
			}

			final IDataIdentifier id = ItemFramework.getDataId(this.stack);

			if (id == null)
			{
				return;
			}

			final RenderFrameworkEditing framework = OrbisClientCaches.getFrameworkRenders().get(id).orNull();

			if (framework == null)
			{
				return;
			}

			GlStateManager.pushMatrix();

			final boolean inGuiContext = OpenGLHelper.isInGuiContext();

			if (!inGuiContext)
			{
				RenderUtil.transformForWorld(framework.getBoundingBox());
				this.setLightmapDisabled(true);
			}
			else
			{
				RenderUtil.transformForGui(framework.getBoundingBox());
			}

			framework.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

			WorldRenderHelp.renderSubRenderers(framework);

			if (!inGuiContext)
			{
				this.setLightmapDisabled(false);
			}

			GlStateManager.resetColor();

			GlStateManager.popMatrix();
		}
		catch (final ExecutionException e)
		{
			OrbisCore.LOGGER.error(e);
		}
	}

	public static class DummyTile extends TileEntityFramework
	{
	}

	@MethodsReturnNonnullByDefault
	@ParametersAreNonnullByDefault
	public class BakedModel implements IBakedModel
	{
		@Override
		public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion()
		{
			return true;
		}

		@Override
		public boolean isGui3d()
		{
			return true;
		}

		@Override
		public boolean isBuiltInRenderer()
		{
			return true;
		}

		@Override
		public TextureAtlasSprite getParticleTexture()
		{
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/soul_sand");
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms()
		{
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides()
		{
			return new Overrides();
		}

		private class Overrides extends ItemOverrideList
		{
			public Overrides()
			{
				super(Collections.emptyList());
			}

			@Override
			public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final EntityLivingBase entity)
			{
				TileEntityFrameworkRenderer.this.stack = stack;
				return BakedModel.this;
			}
		}
	}

}
