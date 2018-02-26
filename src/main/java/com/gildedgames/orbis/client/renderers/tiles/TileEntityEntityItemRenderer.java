package com.gildedgames.orbis.client.renderers.tiles;

import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis.client.renderers.RenderEntityItem;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.items.ItemEntity;
import com.gildedgames.orbis.common.tiles.TileEntityEntityItem;
import com.gildedgames.orbis.common.util.OpenGLHelper;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TileEntityEntityItemRenderer extends TileEntitySpecialRenderer<TileEntityEntityItem>
		implements RemovalListener<EntityEntry, Optional<RenderEntityItem>>
{

	private static final Minecraft mc = Minecraft.getMinecraft();

	public final BakedModel baked = new BakedModel();

	private final LoadingCache<EntityEntry, Optional<RenderEntityItem>> renderCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(this)
			.build(
					new CacheLoader<EntityEntry, Optional<RenderEntityItem>>()
					{
						@Override
						public Optional<RenderEntityItem> load(final EntityEntry entry)
						{
							final RenderEntityItem render = new RenderEntityItem(entry);

							return Optional.of(render);
						}
					});

	private ItemStack stack;

	public TileEntityEntityItemRenderer()
	{

	}

	@Override
	public void render(final TileEntityEntityItem te, final double x, final double y, final double z, final float partialTicks,
			final int destroyStage, float alpha)
	{
		try
		{
			if (this.stack == null)
			{
				return;
			}

			final EntityEntry entry = ItemEntity.getEntityEntry(this.stack);

			if (entry == null)
			{
				return;
			}

			final RenderEntityItem entityItem = this.renderCache.get(entry).orNull();

			if (entityItem == null)
			{
				return;
			}

			GlStateManager.pushMatrix();

			final boolean inGuiContext = OpenGLHelper.isInGuiContext();

			if (!inGuiContext)
			{
				entityItem.transformForWorld();
				this.setLightmapDisabled(true);
			}
			else
			{
				entityItem.transformForGui();
			}

			entityItem.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);

			for (IWorldRenderer renderer : entityItem.getSubRenderers(mc.world))
			{
				renderer.render(mc.world, AirSelectionRenderer.PARTIAL_TICKS, false);
			}

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
	public void onRemoval(RemovalNotification<EntityEntry, Optional<RenderEntityItem>> notification)
	{
		final Optional<RenderEntityItem> opt = notification.getValue();

		if (opt == null)
		{
			return;
		}

		final RenderEntityItem entityItem = opt.orNull();

		if (entityItem != null)
		{
			entityItem.onRemoved();
		}
	}

	public static class DummyTile extends TileEntityEntityItem
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
				TileEntityEntityItemRenderer.this.stack = stack;
				return BakedModel.this;
			}
		}
	}

}
