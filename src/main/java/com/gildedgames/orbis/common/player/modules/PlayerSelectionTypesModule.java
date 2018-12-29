package com.gildedgames.orbis.common.player.modules;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisModule;
import com.gildedgames.orbis.common.network.packets.PacketChangeSelectionType;
import com.gildedgames.orbis.common.player.godmode.selection_types.*;
import com.gildedgames.orbis.player.designer_mode.ISelectionType;
import com.gildedgames.orbis.player.modules.ISelectionTypesModule;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSelectionTypesModule extends PlayerOrbisModule implements ISelectionTypesModule
{
	private final LinkedHashMap<UUID, ISelectionType> uniqueIdToSelectionType = Maps.newLinkedHashMap();

	private final Map<ISelectionType, UUID> selectionTypeToUUID = Maps.newHashMap();

	private final SelectionTypeCuboid cuboid;

	private final SelectionTypeSphere sphere;

	private final SelectionTypeLine line;

	private final SelectionTypePyramid pyramid;

	private final SelectionTypeCone cone;

	private final SelectionTypeCylinder cylinder;

	private final SelectionTypeDome dome;

	private UUID currentUniqueId;

	private ISelectionType currentSelecionType;

	public PlayerSelectionTypesModule(final PlayerOrbis playerOrbis)
	{
		super(playerOrbis);

		this.cuboid = new SelectionTypeCuboid();
		this.sphere = new SelectionTypeSphere();
		this.line = new SelectionTypeLine();
		this.pyramid = new SelectionTypePyramid();
		this.cone = new SelectionTypeCone();
		this.cylinder = new SelectionTypeCylinder();
		this.dome = new SelectionTypeDome();

		this.putSelectionType(UUID.fromString("3ebaf976-501c-431f-8335-0f178e1cb9a2"), this.cuboid);
		this.putSelectionType(UUID.fromString("5fd73afe-29d5-412a-ae2c-e22eb2b984e1"), this.sphere);
		this.putSelectionType(UUID.fromString("9303e161-7f9b-45a0-8261-8480b914b97a"), this.line);
		this.putSelectionType(UUID.fromString("fd33b5ea-1b59-4d58-b41f-751d11951dbb"), this.pyramid);
		this.putSelectionType(UUID.fromString("7fe62bce-250a-4dbc-83b8-630f1fd86dcd"), this.cylinder);
		this.putSelectionType(UUID.fromString("acb9094d-6917-40f5-9bc9-6d02eafc6e69"), this.cone);
		this.putSelectionType(UUID.fromString("b7c55d01-07b5-4e71-b0fb-8eda0d8c2a56"), this.dome);

		this.currentUniqueId = this.selectionTypeToUUID.get(this.cuboid);
	}

	public SelectionTypeDome getDome()
	{
		return this.dome;
	}

	public SelectionTypeCylinder getCylinder()
	{
		return this.cylinder;
	}

	public SelectionTypeCone getCone()
	{
		return this.cone;
	}

	public SelectionTypePyramid getPyramid()
	{
		return this.pyramid;
	}

	public SelectionTypeCuboid getCuboid()
	{
		return this.cuboid;
	}

	public SelectionTypeSphere getSphere()
	{
		return this.sphere;
	}

	public SelectionTypeLine getLine()
	{
		return this.line;
	}

	@Override
	public void putSelectionType(UUID uniqueId, ISelectionType selectionType)
	{
		this.uniqueIdToSelectionType.put(uniqueId, selectionType);
		this.selectionTypeToUUID.put(selectionType, uniqueId);
	}

	@Override
	public void init()
	{
		this.setCurrentSelectionType(this.currentUniqueId);
	}

	@Override
	public ISelectionType getCurrentSelectionType()
	{
		return this.currentSelecionType;
	}

	@Override
	public void setCurrentSelectionType(ISelectionType selectionType)
	{
		this.currentUniqueId = this.selectionTypeToUUID.get(selectionType);
		this.currentSelecionType = selectionType;

		if (this.getWorld().isRemote)
		{
			OrbisCore.network().sendPacketToServer(new PacketChangeSelectionType(this.currentUniqueId));
		}
	}

	@Override
	public void setCurrentSelectionType(UUID uniqueId)
	{
		if (!this.uniqueIdToSelectionType.containsKey(uniqueId))
		{
			OrbisCore.LOGGER
					.error("A unique id was provided when setting the current selection type that doesn't exist in the selection types module! Ignoring.");
			return;
		}

		this.currentUniqueId = uniqueId;
		this.currentSelecionType = this.uniqueIdToSelectionType.get(this.currentUniqueId);

		if (this.getWorld().isRemote)
		{
			OrbisCore.network().sendPacketToServer(new PacketChangeSelectionType(this.currentUniqueId));
		}
	}

	@Override
	public ISelectionType getSelectionType(UUID uniqueId)
	{
		return this.uniqueIdToSelectionType.get(uniqueId);
	}

	@Override
	public UUID getUniqueId(ISelectionType selectionType)
	{
		return this.selectionTypeToUUID.get(selectionType);
	}

	@Override
	public void removeSelectionType(UUID uniqueId)
	{
		if (this.selectionTypeToUUID.size() <= 1)
		{
			throw new IllegalStateException("Tried to remove a selection type from the player module when the amount of selection types is 1 or less.");
		}

		ISelectionType type = this.uniqueIdToSelectionType.remove(uniqueId);
		this.selectionTypeToUUID.remove(type);

		if (this.currentUniqueId.equals(uniqueId))
		{
			this.setCurrentSelectionType(this.selectionTypeToUUID.values().iterator().next());
		}
	}

	@Override
	public Collection<ISelectionType> getSelectionTypes()
	{
		return this.uniqueIdToSelectionType.values();
	}

	@Override
	public void onUpdate()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		tag.setUniqueId("currentUniqueId", this.currentUniqueId);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		if (tag.hasUniqueId("currentUniqueId"))
		{
			UUID uniqueId = tag.getUniqueId("currentUniqueId");

			if (uniqueId != null)
			{
				this.currentUniqueId = uniqueId;
			}
		}
	}
}
