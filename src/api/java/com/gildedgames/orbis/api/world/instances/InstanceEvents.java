package com.gildedgames.orbis.api.world.instances;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber()
public class InstanceEvents
{

	private InstanceEvents()
	{

	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event)
	{
		for (final IInstanceHandler<?> handler : OrbisAPI.instances().getInstanceHandlers())
		{
			handler.sendUnregisterInstancesPacket((EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public static void onPlayerTravel(EntityTravelToDimensionEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			final IPlayerInstances hook = OrbisAPI.instances().getPlayer((EntityPlayer) event.getEntity());

			if (hook.getInstance() != null)
			{
				hook.getInstance().onLeave((EntityPlayer) event.getEntity());
			}
		}
	}

	public static void unregisterAllInstances()
	{
		OrbisAPI.instances().getInstanceHandlers().forEach(IInstanceHandler::unregisterInstances);
	}

	public static void loadAllInstancesFromDisk()
	{
		final NBTTagCompound tag = NBTHelper.readNBTFromFile("//data//instances.dat");

		if (tag == null)
		{
			return;
		}

		int i = 0;

		for (final IInstanceHandler<?> handler : OrbisAPI.instances().getInstanceHandlers())
		{
			final NBTTagCompound subTag = tag.getCompoundTag(String.valueOf(i++));

			handler.read(subTag);
		}
	}

	public static void saveAllInstancesToDisk()
	{
		final NBTTagCompound tag = new NBTTagCompound();

		int i = 0;

		tag.setInteger("size", OrbisAPI.instances().getInstanceHandlers().size());

		for (final IInstanceHandler<?> handler : OrbisAPI.instances().getInstanceHandlers())
		{
			final NBTTagCompound subTag = new NBTTagCompound();
			handler.write(subTag);

			tag.setTag(String.valueOf(i++), subTag);
		}

		NBTHelper.writeNBTToFile(tag, "//data//instances.dat");
	}

}
