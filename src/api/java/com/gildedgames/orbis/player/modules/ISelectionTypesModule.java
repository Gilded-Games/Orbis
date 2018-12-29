package com.gildedgames.orbis.player.modules;

import com.gildedgames.orbis.player.designer_mode.ISelectionType;

import java.util.Collection;
import java.util.UUID;

public interface ISelectionTypesModule
{
	void init();

	ISelectionType getCurrentSelectionType();

	void setCurrentSelectionType(ISelectionType selectionType);

	void setCurrentSelectionType(UUID uniqueId);

	ISelectionType getSelectionType(UUID uniqueId);

	UUID getUniqueId(ISelectionType selectionType);

	void putSelectionType(UUID uniqueId, ISelectionType selectionType);

	void removeSelectionType(UUID uniqueId);

	Collection<ISelectionType> getSelectionTypes();
}
