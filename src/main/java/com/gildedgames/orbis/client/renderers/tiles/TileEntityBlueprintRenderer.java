package com.gildedgames.orbis.client.renderers.tiles;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis.client.renderers.RenderBlueprintBlocks;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.tiles.TileEntityBlueprint;
import com.gildedgames.orbis.common.util.OpenGLHelper;
import com.gildedgames.orbis.common.util.WorldRenderHelp;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.base.Optional;
import com.google.common.cache.*;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TileEntityBlueprintRenderer extends TileEntitySpecialRenderer<TileEntityBlueprint>
		implements RemovalListener<IDataIdentifier, Optional<RenderBlueprintBlocks>>
{

	private static final Minecraft mc = Minecraft.getMinecraft();

	public final BakedModel baked = new BakedModel();

	private final LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> blueprintCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(this)
			.build(
					new CacheLoader<IDataIdentifier, Optional<RenderBlueprintBlocks>>()
					{
						@Override
						public Optional<RenderBlueprintBlocks> load(final IDataIdentifier id)
						{
							try
							{
								final BlueprintData blueprint = OrbisCore.getProjectManager().findData(id);

								final RenderBlueprintBlocks render = new RenderBlueprintBlocks(new Blueprint(mc.world, BlockPos.ORIGIN, blueprint), mc.world);

								return Optional.of(render);
							}
							catch (final OrbisMissingDataException | OrbisMissingProjectException e)
							{
								OrbisCore.LOGGER.error("Missing in " + TileEntityBlueprintRenderer.class.getName() + " : ", e);
							}

							return Optional.absent();
						}
					});

	private ItemStack stack;

	public TileEntityBlueprintRenderer()
	{

	}

	@Override
	public void render(final TileEntityBlueprint te, final double x, final double y, final double z, final float partialTicks,
			final int destroyStage, float alpha)
	{
		try
		{
			if (this.stack == null)
			{
				return;
			}

			final IDataIdentifier id = ItemBlueprint.getBlueprintId(this.stack);

			if (id == null)
			{
				return;
			}

			final RenderBlueprintBlocks blueprint = this.blueprintCache.get(id).orNull();

			if (blueprint == null)
			{
				return;
			}

			GlStateManager.pushMatrix();

			final boolean inGuiContext = OpenGLHelper.isInGuiContext();

			if (!inGuiContext)
			{
				blueprint.transformForWorld();
				this.setLightmapDisabled(true);
			}
			else
			{
				blueprint.transformForGui();
			}

			blueprint.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

			WorldRenderHelp.renderSubRenderers(blueprint);

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

	@Override
	public void onRemoval(final RemovalNotification<IDataIdentifier, Optional<RenderBlueprintBlocks>> notification)
	{
		final Optional<RenderBlueprintBlocks> opt = notification.getValue();

		if (opt == null)
		{
			return;
		}

		final RenderBlueprintBlocks blueprint = opt.orNull();

		if (blueprint != null)
		{
			blueprint.onRemoved();
		}
	}

	public static class DummyTile extends TileEntityBlueprint
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
				TileEntityBlueprintRenderer.this.stack = stack;
				return BakedModel.this;
			}
		}
	}

}
