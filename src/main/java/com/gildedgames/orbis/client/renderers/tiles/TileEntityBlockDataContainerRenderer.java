package com.gildedgames.orbis.client.renderers.tiles;

import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis.client.renderers.RenderBlueprintBlocks;
import com.gildedgames.orbis.client.renderers.RenderUtil;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.tiles.TileEntityBlockDataContainer;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.util.OpenGLHelper;
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TileEntityBlockDataContainerRenderer extends TileEntitySpecialRenderer<TileEntityBlockDataContainer>
		implements RemovalListener<BlockDataContainer, Optional<RenderBlueprintBlocks>>
{

	private static final Minecraft mc = Minecraft.getMinecraft();

	public final BakedModel baked = new BakedModel();

	private final LoadingCache<BlockDataContainer, Optional<RenderBlueprintBlocks>> blueprintCache = CacheBuilder.newBuilder()
			.maximumSize(200)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(this)
			.build(
					new CacheLoader<BlockDataContainer, Optional<RenderBlueprintBlocks>>()
					{
						@Override
						public Optional<RenderBlueprintBlocks> load(final BlockDataContainer container)
						{
							final RenderBlueprintBlocks blueprint = new RenderBlueprintBlocks(
									new Blueprint(mc.world, BlockPos.ORIGIN, new BlueprintData(container)),
									mc.world);

							return Optional.of(blueprint);
						}
					});

	private ItemStack stack;

	public TileEntityBlockDataContainerRenderer()
	{

	}

	@Override
	public void render(final TileEntityBlockDataContainer te, final double x, final double y, final double z, final float partialTicks,
			final int destroyStage, float alpha)
	{
		try
		{
			if (this.stack == null)
			{
				return;
			}

			final Optional<BlockDataContainer> container = ItemBlockDataContainer.getDataContainer(this.stack);

			if (!container.isPresent())
			{
				return;
			}

			final RenderBlueprintBlocks blueprint = this.blueprintCache.get(container.get()).orElse(null);

			if (blueprint == null)
			{
				return;
			}

			GlStateManager.pushMatrix();

			final boolean inGuiContext = OpenGLHelper.isInGuiContext();

			if (!inGuiContext)
			{
				RenderUtil.transformForWorld(blueprint.getBoundingBox());
				this.setLightmapDisabled(true);
			}
			else
			{
				RenderUtil.transformForGui(blueprint.getBoundingBox());
			}

			blueprint.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

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
	public void onRemoval(final RemovalNotification<BlockDataContainer, Optional<RenderBlueprintBlocks>> notification)
	{
		final Optional<RenderBlueprintBlocks> opt = notification.getValue();

		if (opt == null)
		{
			return;
		}

		opt.ifPresent(RenderBlueprintBlocks::onRemoved);
	}

	public static class DummyTile extends TileEntityBlockDataContainer
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
				TileEntityBlockDataContainerRenderer.this.stack = stack;
				return BakedModel.this;
			}
		}
	}

}
