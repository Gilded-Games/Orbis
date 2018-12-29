package com.gildedgames.orbis;

import com.gildedgames.orbis.packs.IOrbisPackData;
import com.gildedgames.orbis.scripting.IScriptingManager;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;

public interface IOrbisServices
{
	boolean isPackDataTypeRegistered(String type);

	Class<? extends IOrbisPackData> getPackDataType(String type);

	<T extends IOrbisPackData> void registerPackDataType(String type, Class<T> clazz, JsonDeserializer<T> deserializer);

	IScriptingManager scripting();

	Gson getGson();

	void listen(IOrbisServicesListener listener);
}
