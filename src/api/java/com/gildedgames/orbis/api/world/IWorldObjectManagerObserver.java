package com.gildedgames.orbis.api.world;


public interface IWorldObjectManagerObserver
{

	void onGroupAdded(WorldObjectManager manager, IWorldObjectGroup group);

	void onGroupRemoved(WorldObjectManager manager, IWorldObjectGroup group);

	void onReloaded(WorldObjectManager manager);

}
