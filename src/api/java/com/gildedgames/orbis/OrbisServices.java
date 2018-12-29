package com.gildedgames.orbis;

import com.gildedgames.orbis.packs.IOrbisPack;
import com.gildedgames.orbis.packs.IOrbisPackData;
import com.gildedgames.orbis.packs.OrbisPack;
import com.gildedgames.orbis.packs.OrbisPackDataDeserializer;
import com.gildedgames.orbis.scripting.IScriptingManager;
import com.gildedgames.orbis.scripting.impl.ScriptingManager;
import com.gildedgames.orbis.scripting.impl.lua.ScriptingEngineLua;
import com.gildedgames.orbis_api.data.management.impl.GenericSerializer;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

public class OrbisServices implements IOrbisServices
{
	private final HashMap<String, Class<? extends IOrbisPackData>> dataTypes = new HashMap<>();

	private List<Pair<Class<?>, JsonDeserializer<?>>> pendingGsonRegistrations = Lists.newArrayList();

	private IScriptingManager scriptingManager;

	private Gson gson;

	private List<IOrbisServicesListener> listeners = Lists.newArrayList();

	public OrbisServices()
	{
		this.scriptingManager = new ScriptingManager();

		this.scriptingManager.register(new ScriptingEngineLua());
	}

	public void buildGson()
	{
		if (this.gson != null)
		{
			return;
		}

		GsonBuilder builder = new GsonBuilder()
				.registerTypeAdapter(IOrbisPack.class, new GenericSerializer<IOrbisPack>(OrbisPack.class));

		builder.registerTypeAdapter(IOrbisPackData.class, new OrbisPackDataDeserializer());

		this.pendingGsonRegistrations.forEach((p) -> builder.registerTypeAdapter(p.getKey(), p.getValue()));

		this.listeners.forEach((l) -> l.onGsonBuild(builder));

		this.gson = builder.create();

		this.pendingGsonRegistrations = null;
	}

	@Override
	public boolean isPackDataTypeRegistered(String type)
	{
		return this.dataTypes.containsKey(type);
	}

	@Override
	public Class<? extends IOrbisPackData> getPackDataType(String type)
	{
		return this.dataTypes.get(type);
	}

	@Override
	public <T extends IOrbisPackData> void registerPackDataType(String type, Class<T> clazz, JsonDeserializer<T> deserializer)
	{
		this.dataTypes.put(type, clazz);

		this.pendingGsonRegistrations.add(Pair.of(clazz, deserializer));
	}

	@Override
	public IScriptingManager scripting()
	{
		return this.scriptingManager;
	}

	@Override
	public Gson getGson()
	{
		return this.gson;
	}

	@Override
	public void listen(IOrbisServicesListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			return;
		}

		this.listeners.add(listener);
	}
}
