package com.gildedgames.orbis.packs;

import com.gildedgames.orbis.player.IPlayerOrbis;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipFile;

public interface IOrbisPackData
{
	/**
	 * Assemble all dependencies from the provided packFile
	 * then cache the dependent data.
	 * @param packFile
	 */
	void assembleDependencies(ZipFile packFile) throws IOException;

	UUID getUniqueId();

	void enableData(IPlayerOrbis playerOrbis);

	void disableData(IPlayerOrbis playerOrbis);
}
