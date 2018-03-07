package com.gildedgames.orbis.api.world;

public interface IWorldObjectManagerObserver
{

	void onObjectAdded(WorldObjectManager manager, IWorldObject obj);

	void onObjectRemoved(WorldObjectManager manager, IWorldObject obj);

	void onReloaded(WorldObjectManager manager);

}
