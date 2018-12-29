package com.gildedgames.orbis;

public class OrbisAPI
{
	private static OrbisServices services;

	public static IOrbisServices services()
	{
		if (OrbisAPI.services == null)
		{
			OrbisAPI.services = new OrbisServices();
		}

		return OrbisAPI.services;
	}

	public static void onPostFMLInit()
	{
		services.buildGson();
	}
}
