package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.client.OrbisKeyBindings;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerCreative;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis.common.world_objects.WorldRegion;
import com.gildedgames.orbis_api.data.region.IMutableRegion;
import com.gildedgames.orbis_api.world.IWorldObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class AirSelectionRenderer
{

	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final float fadeMax = 0.15F;

	public static float PARTIAL_TICKS;

	private static long lastTimeRegionAlphaChanged;

	private static double prevReach;

	private static float timeDifRequired, timeToFade = 500.0F, fadeMin;

	private static float startedFrom;

	private static int prevSlotIndex;

	private static IMutableRegion region;

	private static RenderShape renderRegion;

	private static BlockPos prevPos;

	private AirSelectionRenderer()
	{

	}

	private static void resetRegionAlpha()
	{
		lastTimeRegionAlphaChanged = System.currentTimeMillis();

		startedFrom = renderRegion.boxAlpha;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRenderWorldLast(final RenderWorldLastEvent event)
	{
		PARTIAL_TICKS = event.getPartialTicks();

		final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);

		final EntityPlayer entity = playerOrbis.getEntity();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		Object foundObject = playerOrbis.getSelectedRegion(Framework.class);
		boolean playerInside = false;

		if (foundObject instanceof IWorldObject)
		{
			IWorldObject obj = (IWorldObject) foundObject;

			playerInside = obj.getShape().contains(x, y, z) || obj.getShape().contains(x, MathHelper.floor(entity.posY + entity.height), z);
		}

		boolean scheduleHover = playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getSelectPower() && playerOrbis
				.powers().isScheduling() && playerOrbis.getSelectedSchedule() != null;
		boolean blueprintHover = playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getBlueprintPower()
				&& playerOrbis.getSelectedRegion() != null && !playerInside;
		boolean entranceHover = playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getEntrancePower() && playerOrbis.getSelectedEntrance() != null;
		boolean frameworkHover =
				playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getFrameworkPower() && playerOrbis.getSelectedRegion() != null && !playerInside;
		boolean nodeHover = playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getFrameworkPower() && playerOrbis.getSelectedNode() != null;

		if (!playerOrbis.inDeveloperMode() || scheduleHover || blueprintHover || entranceHover || nodeHover || frameworkHover)
		{
			return;
		}

		final BlockPos airRaytrace = playerOrbis.raytraceNoSnapping();

		if (!(playerOrbis.powers().getCurrentPower() instanceof GodPowerCreative))
		{
			//Minecraft.getMinecraft().objectMouseOver = new RayTraceResult(mc.player, mc.player.getLook(1.0F));
		}

		if (region == null || renderRegion == null)
		{
			region = new WorldRegion(new BlockPos(0, 0, 0), mc.world);
			renderRegion = new RenderShape(region);

			renderRegion.box = false;
			renderRegion.useCustomColors = true;
			renderRegion.renderDimensionsAbove = false;

			renderRegion.boxAlpha = fadeMax;
		}

		final IGodPowerClient powerClient = playerOrbis.powers().getCurrentPower().getClientHandler();

		final boolean has3DCursor =
				powerClient.has3DCursor(playerOrbis) || playerOrbis.getEntity().getHeldItemMainhand().getItem() instanceof IShapeSelector;

		if (!has3DCursor)
		{
			fadeMin = powerClient.minFade3DCursor(playerOrbis);
		}
		else
		{
			fadeMin = 0.05F;
		}

		if (prevSlotIndex != mc.player.inventory.currentItem)
		{
			resetRegionAlpha();

			timeDifRequired = 0.0F;
			timeToFade = 500.0F;

			prevSlotIndex = mc.player.inventory.currentItem;
		}

		final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

		renderRegion.colorBorder = 0xFFFFFF;
		renderRegion.colorGrid = 0xFFFFFF;

		if (!pos.equals(prevPos) && has3DCursor)
		{
			resetRegionAlpha();

			renderRegion.boxAlpha = fadeMax;

			timeDifRequired = 700.0F;
			timeToFade = 800.0F;

			prevPos = pos;
		}

		if (prevReach != playerOrbis.getDeveloperReach())
		{
			resetRegionAlpha();

			renderRegion.boxAlpha = fadeMax;
			startedFrom = fadeMax;

			timeDifRequired = 0.0F;
			timeToFade = 500.0F;

			prevReach = playerOrbis.getDeveloperReach();
		}

		region.setBounds(pos, pos);

		final float timeDif = System.currentTimeMillis() - lastTimeRegionAlphaChanged;
		final float timeMinusReq = timeDif - timeDifRequired;

		if (timeMinusReq > 0.0F && renderRegion.boxAlpha > fadeMin)
		{
			final float fadeProgress = 1.0F - (timeMinusReq / timeToFade);

			renderRegion.boxAlpha = Math.max(fadeMin, fadeProgress * startedFrom);
		}

		if (timeMinusReq < 0.0F && renderRegion.boxAlpha < fadeMax)
		{
			final float fadeProgress = 1.0F + (timeDif / timeToFade);

			//renderRegion.boxAlpha = Math.min(fadeMax, fadeProgress * fadeMin);
		}

		if (renderRegion.boxAlpha < fadeMin)
		{
			renderRegion.boxAlpha = fadeMin;
		}

		if (renderRegion.boxAlpha > fadeMax)
		{
			renderRegion.boxAlpha = fadeMax;
		}

		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();

		final int color = 0xFFFFFF;

		renderRegion.colorBorder = color;
		renderRegion.colorBorder = color;

		renderRegion.box = false;
		renderRegion.xyz_box =
				renderRegion.boxAlpha > 0 && OrbisKeyBindings.keyBindControl.isKeyDown()
						&& playerOrbis.selectionInputs().getCurrentSelectionInput().getActiveSelection() == null;

		renderRegion.renderFully(mc.world, PARTIAL_TICKS, true);
		renderRegion.onRemoved();

		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

}
