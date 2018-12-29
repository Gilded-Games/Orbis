package com.gildedgames.orbis.packs;

import com.gildedgames.orbis.OrbisAPI;
import com.google.gson.*;

import java.lang.reflect.Type;

public class OrbisPackDataDeserializer implements JsonDeserializer<IOrbisPackData>
{
	public OrbisPackDataDeserializer()
	{

	}

	@Override
	public IOrbisPackData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
	{
		final JsonObject root = json.getAsJsonObject();

		if (!root.has("type"))
		{
			throw new JsonParseException("Missing required field 'type' for pack data");
		}

		final String type = root.get("type").getAsString();

		if (!OrbisAPI.services().isPackDataTypeRegistered(type))
		{
			throw new JsonParseException("Invalid pack data type " + type);
		}

		return context.deserialize(json, OrbisAPI.services().getPackDataType(type));
	}
}
