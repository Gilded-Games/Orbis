package com.gildedgames.orbis.api.world;

public interface IWorldObjectGroupObserver
{

	void onObjectAdded(IWorldObjectGroup group, IWorldObject object);

	void onObjectRemoved(IWorldObjectGroup group, IWorldObject object);

	void onReloaded(IWorldObjectGroup group);

}
