package com.gildedgames.orbis.api;

import com.gildedgames.orbis.api.core.GameRegistrar;
import com.gildedgames.orbis.api.core.registry.IOrbisDefinitionRegistry;
import com.gildedgames.orbis.api.data.management.IProject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public interface IOrbisServices
{

	GameRegistrar registrar();

	Logger log();

	/**
	 * Searches for the definition registry linked with the
	 * provided registry id. If it cannot find it, it will
	 * return null.
	 * @param registryId The unique registry id associated
	 *                   with the definition registry you're
	 *                   attempting to find.
	 */
	@Nullable
	IOrbisDefinitionRegistry findDefinitionRegistry(String registryId);

	/**
	 *
	 * @param registry The registry you're registering.
	 */
	void register(IOrbisDefinitionRegistry registry);

	/**
	 * Loads a project with the provided resource location.
	 * @param location The location of the project.
	 * @return The loaded project.
	 */
	IProject loadProject(MinecraftServer server, ResourceLocation location);

	IOHelper io();

}
