package com.gildedgames.orbis.common.capabilities.player;

import com.gildedgames.orbis.common.OrbisCapabilities;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.*;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.modules.PlayerPowerModule;
import com.gildedgames.orbis.common.player.modules.PlayerProjectModule;
import com.gildedgames.orbis.common.player.modules.PlayerSelectionInputModule;
import com.gildedgames.orbis.common.player.modules.PlayerSelectionTypesModule;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstance;
import com.gildedgames.orbis.common.world_actions.IWorldActionLog;
import com.gildedgames.orbis.common.world_actions.WorldActionLog;
import com.gildedgames.orbis.common.world_actions.WorldActionLogClient;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.pathway.IEntrance;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.util.mc.NBTHelper;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class PlayerOrbis implements IPlayerOrbis
{
	private final EntityPlayer entity;

	private final PlayerPowerModule godPowerModule;

	private final PlayerSelectionTypesModule selectionTypeModule;

	private final PlayerProjectModule projectModule;

	private final PlayerSelectionInputModule selectionInputModule;

	private final List<PlayerOrbisModule> modules = Lists.newArrayList();

	private final List<PlayerOrbisObserver> observers = Lists.newArrayList();

	private double developerReach = 5.0D;

	private boolean developerModeEnabled;

	private IShape selectedRegion;

	private IFrameworkNode selectedNode;

	private ISchedule selectedSchedule;

	private IEntrance selectedEntrance;

	private OrbisInstance orbisInstance;

	private IWorldActionLog worldActionLog = new WorldActionLog(this, 20);

	private IWorldActionLog worldActionLogClient = new WorldActionLogClient();

	public PlayerOrbis()
	{
		this.entity = null;

		this.godPowerModule = null;
		this.selectionTypeModule = null;
		this.projectModule = null;
		this.selectionInputModule = null;
	}

	public PlayerOrbis(final EntityPlayer entity)
	{
		this.entity = entity;

		this.godPowerModule = new PlayerPowerModule(this);
		this.selectionTypeModule = new PlayerSelectionTypesModule(this);
		this.projectModule = new PlayerProjectModule(this);
		this.selectionInputModule = new PlayerSelectionInputModule(this);

		this.modules.add(this.godPowerModule);
		this.modules.add(this.selectionTypeModule);
		this.modules.add(this.projectModule);
		this.modules.add(this.selectionInputModule);
	}

	public static PlayerOrbis get(final Entity player)
	{
		if (!PlayerOrbis.hasCapability(player))
		{
			return null;
		}

		return (PlayerOrbis) player.getCapability(OrbisCapabilities.PLAYER_DATA, null);
	}

	public static boolean hasCapability(final Entity entity)
	{
		return entity.hasCapability(OrbisCapabilities.PLAYER_DATA, null);
	}

	public OrbisInstance getOrbisInstance()
	{
		return this.orbisInstance;
	}

	public void setOrbisInstance(OrbisInstance orbisInstance)
	{
		this.orbisInstance = orbisInstance;
	}

	public boolean containsObserver(final PlayerOrbisObserver observer)
	{
		return this.observers.contains(observer);
	}

	public void addObserver(final PlayerOrbisObserver observer)
	{
		this.observers.add(observer);
	}

	public boolean removeObserver(final PlayerOrbisObserver observer)
	{
		return this.observers.remove(observer);
	}

	public IWorldActionLog getWorldActionLog()
	{
		if (this.getWorld().isRemote)
		{
			return this.worldActionLogClient;
		}

		return this.worldActionLog;
	}

	/**
	 * Syncs the client and watching entities completely.
	 */
	public void sendFullUpdate()
	{
		OrbisCore.network().sendPacketToPlayer(new PacketDeveloperMode(this.inDeveloperMode()), (EntityPlayerMP) this.getEntity());
		OrbisCore.network()
				.sendPacketToPlayer(new PacketChangePower(this.powers().getCurrentPowerIndex()), (EntityPlayerMP) this.getEntity());
		OrbisCore.network()
				.sendPacketToPlayer(new PacketChangeSelectionInput(this, this.selectionInputs().getCurrentSelectionInput()),
						(EntityPlayerMP) this.getEntity());
		OrbisCore.network()
				.sendPacketToPlayer(new PacketChangeSelectionType(this, this.selectionTypes().getCurrentSelectionType()),
						(EntityPlayerMP) this.getEntity());
		OrbisCore.network()
				.sendPacketToPlayer(new PacketSetScheduling(this.powers().isScheduling()),
						(EntityPlayerMP) this.getEntity());
	}

	public void onRespawn(final PlayerEvent.PlayerRespawnEvent event)
	{
		this.sendFullUpdate();
	}

	public void onTeleport(final PlayerEvent.PlayerChangedDimensionEvent event)
	{
		this.sendFullUpdate();

		if (!this.getWorld().isRemote)
		{
			this.getWorldActionLog().clear();
		}
	}

	public void onPlayerBeginWatching(final IPlayerOrbis other)
	{
		OrbisCore.network().sendPacketToPlayer(new PacketStagedInventoryChanged(this, this.powers().getBlueprintPower().getStagedInventory()),
				(EntityPlayerMP) other.getEntity());
		OrbisCore.network().sendPacketToPlayer(new PacketStagedInventoryChanged(this, this.powers().getFillPower().getStagedInventory()),
				(EntityPlayerMP) other.getEntity());
	}

	public PlayerSelectionInputModule selectionInputs()
	{
		return this.selectionInputModule;
	}

	public PlayerProjectModule projects()
	{
		return this.projectModule;
	}

	public PlayerPowerModule powers()
	{
		return this.godPowerModule;
	}

	public PlayerSelectionTypesModule selectionTypes()
	{
		return this.selectionTypeModule;
	}

	public List<IWorldRenderer> getActiveRenderers()
	{
		if (!this.inDeveloperMode())
		{
			return Collections.emptyList();
		}

		final List<IWorldRenderer> renderers = Lists.newArrayList();

		for (final IGodPower power : this.powers().array())
		{
			renderers.addAll(power.getClientHandler().getActiveRenderers(this, this.getWorld()));
		}

		for (final ISelectionInput input : this.selectionInputs().array())
		{
			if (input == this.selectionInputs().getCurrentSelectionInput())
			{
				renderers.addAll(input.getClient().getActiveRenderers(input, this, this.getWorld()));
			}
		}

		return renderers;
	}

	public boolean canInteractWithItems()
	{
		return this.powers().getCurrentPower().canInteractWithItems(this);
	}

	public boolean inDeveloperMode()
	{
		return this.developerModeEnabled;
	}

	public IShape getSelectedRegion()
	{
		return this.selectedRegion;
	}

	public <T extends IShape> T getSelectedRegion(Class<T> clazz)
	{
		if (this.selectedRegion != null && this.selectedRegion.getClass() == clazz)
		{
			return (T) this.selectedRegion;
		}

		return null;
	}

	public IFrameworkNode getSelectedNode()
	{
		return this.selectedNode;
	}

	public ISchedule getSelectedSchedule()
	{
		return this.selectedSchedule;
	}

	public IEntrance getSelectedEntrance()
	{
		return this.selectedEntrance;
	}

	public void setDeveloperMode(final boolean flag)
	{
		this.developerModeEnabled = flag;

		if (!this.getEntity().world.isRemote)
		{
			OrbisCore.network().sendPacketToPlayer(new PacketDeveloperMode(flag), (EntityPlayerMP) this.getEntity());
		}
	}

	@SideOnly(Side.CLIENT)
	public BlockPos raytraceNoSnapping()
	{
		return OrbisRaytraceHelp.raytraceNoSnapping(this.getEntity());
	}

	@SideOnly(Side.CLIENT)
	public double getReach()
	{
		final boolean creativeMode = this.getEntity().capabilities.isCreativeMode;

		if (this.inDeveloperMode())
		{
			return OrbisRaytraceHelp.getFinalExtendedReach(this.getEntity(), this.developerReach);
		}
		else
		{
			return creativeMode ? 5.0F : 4.5F;
		}
	}

	public double getDeveloperReach()
	{
		return this.developerReach;
	}

	@SideOnly(Side.CLIENT)
	public void setDeveloperReach(final double reach)
	{
		this.developerReach = Math.max(1, reach);
	}

	@Override
	public void onUpdate(LivingUpdateEvent event)
	{
		this.selectedRegion = OrbisRaytraceHelp.raytraceLocateObject(this.getEntity(), 1, OrbisRaytraceHelp.WORLD_OBJECT_LOCATOR);
		this.selectedNode = OrbisRaytraceHelp.raytraceLocateObject(this.getEntity(), 1, OrbisRaytraceHelp.FRAMEWORK_NODE_LOCATOR);
		this.selectedSchedule = OrbisRaytraceHelp.raytraceLocateObject(this.getEntity(), 1, OrbisRaytraceHelp.SCHEDULE_LOCATOR);
		this.selectedEntrance = OrbisRaytraceHelp.raytraceLocateObject(this.getEntity(), 1, OrbisRaytraceHelp.ENTRANCE_LOCATOR);

		for (final PlayerOrbisModule module : this.modules)
		{
			module.onUpdate();
		}

		for (final PlayerOrbisObserver observer : this.observers)
		{
			observer.onUpdate(this);
		}
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setBoolean("developerModeEnabled", this.developerModeEnabled);

		funnel.set("orbisInstance", this.orbisInstance);

		final NBTTagList modules = new NBTTagList();

		for (final PlayerOrbisModule module : this.modules)
		{
			modules.appendTag(NBTHelper.writeRaw(module));
		}

		tag.setTag("modules", modules);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.developerModeEnabled = tag.getBoolean("developerModeEnabled");

		OrbisInstance inst = funnel.get("orbisInstance");

		if (inst != null)
		{
			this.orbisInstance = inst;
		}

		final NBTTagList modules = tag.getTagList("modules", 10);

		for (int i = 0; i < modules.tagCount(); i++)
		{
			if (i < this.modules.size())
			{
				final PlayerOrbisModule module = this.modules.get(i);

				module.read(modules.getCompoundTagAt(i));
			}
		}
	}

	@Override
	public World getWorld()
	{
		return this.getEntity().getEntityWorld();
	}

	@Override
	public EntityPlayer getEntity()
	{
		return this.entity;
	}

	public static class Storage implements Capability.IStorage<IPlayerOrbis>
	{
		@Override
		public NBTBase writeNBT(final Capability<IPlayerOrbis> capability, final IPlayerOrbis instance, final EnumFacing side)
		{
			final NBTTagCompound compound = new NBTTagCompound();
			instance.write(compound);

			return compound;
		}

		@Override
		public void readNBT(final Capability<IPlayerOrbis> capability, final IPlayerOrbis instance, final EnumFacing side, final NBTBase nbt)
		{
			final NBTTagCompound compound = (NBTTagCompound) nbt;

			instance.read(compound);
		}
	}
}
