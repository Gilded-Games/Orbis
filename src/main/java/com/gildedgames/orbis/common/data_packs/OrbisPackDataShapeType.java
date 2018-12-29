package com.gildedgames.orbis.common.data_packs;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.player.godmode.selection_types.SelectionTypeScript;
import com.gildedgames.orbis.packs.IOrbisPackData;
import com.gildedgames.orbis.player.IPlayerOrbis;
import com.gildedgames.orbis.player.designer_mode.ISelectionType;
import com.gildedgames.orbis_api.OrbisLib;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OrbisPackDataShapeType implements IOrbisPackData
{
	private UUID uniqueId;

	private String shapeName, inequalityMathExpression, iconLocation;

	private BufferedImage iconBuffer;

	private DynamicTexture icon;

	private ResourceLocation iconResourceLocation;

	public OrbisPackDataShapeType(UUID uniqueId, String shapeName, String inequalityMathExpression, String iconLocation)
	{
		Validate.notNull(uniqueId, "Unique ID cannot be null.");
		Validate.notNull(shapeName, "Shape name cannot be null.");
		Validate.notNull(inequalityMathExpression, "Inequality math expression cannot be null.");
		Validate.notNull(iconLocation, "Icon location cannot be null");

		this.uniqueId = uniqueId;
		this.shapeName = shapeName;
		this.inequalityMathExpression = inequalityMathExpression;
		this.iconLocation = iconLocation;
	}

	@Override
	public void assembleDependencies(ZipFile packFile) throws IOException
	{
		ZipEntry entry = packFile.getEntry(this.iconLocation);

		if (entry != null)
		{
			try (InputStream inputStream = packFile.getInputStream(entry))
			{
				try
				{
					this.iconBuffer = TextureUtil.readBufferedImage(inputStream);
				}
				catch (Throwable throwable)
				{
					OrbisCore.LOGGER.error("Invalid icon for shape type {}", this.uniqueId, this.shapeName, throwable);
					this.icon = null;
				}
			}
		}
		else
		{
			OrbisLib.LOGGER.error("Could not load icon for ShapeType Orbis Pack", this.iconLocation);
		}

		/*ZipEntry entry = packFile.getEntry(this.inequalityMathExpression);

		if (entry != null)
		{
			try (InputStream inputStream = packFile.getInputStream(entry); InputStreamReader reader = new InputStreamReader(inputStream))
			{
				Optional<IScriptingEngine> engine = OrbisAPI.services().scripting().getEngineFromExtension("lua");

				engine.ifPresent(iScriptingEngine -> this.script = iScriptingEngine.compile(reader));
			}
		}
		else
		{
			OrbisLib.LOGGER.error("Could not load script for ShapeType Orbis Pack", this.inequalityMathExpression);
		}*/
	}

	@Override
	public UUID getUniqueId()
	{
		return this.uniqueId;
	}

	@Override
	public void enableData(IPlayerOrbis playerOrbis)
	{
		if (playerOrbis.getWorld().isRemote)
		{
			if (this.icon == null)
			{
				this.iconResourceLocation = new ResourceLocation(this.uniqueId.toString(), this.iconLocation);

				Minecraft.getMinecraft().getTextureManager().deleteTexture(this.iconResourceLocation);

				this.icon = new DynamicTexture(this.iconBuffer.getWidth(), this.iconBuffer.getHeight());
				Minecraft.getMinecraft().getTextureManager().loadTexture(this.iconResourceLocation, this.icon);
			}

			this.iconBuffer.getRGB(0, 0, this.iconBuffer.getWidth(), this.iconBuffer.getHeight(), this.icon.getTextureData(), 0, this.iconBuffer.getWidth());
			this.icon.updateDynamicTexture();
		}

		ISelectionType type = new SelectionTypeScript(this.uniqueId, this.shapeName, this.inequalityMathExpression, this.iconResourceLocation);

		playerOrbis.selectionTypes().putSelectionType(this.uniqueId, type);
	}

	@Override
	public void disableData(IPlayerOrbis playerOrbis)
	{
		if (playerOrbis.getWorld().isRemote)
		{
			Minecraft.getMinecraft().getTextureManager().deleteTexture(this.iconResourceLocation);
			this.icon = null;
		}

		playerOrbis.selectionTypes().removeSelectionType(this.uniqueId);
	}

	public static class Deserializer implements JsonDeserializer<OrbisPackDataShapeType>
	{
		@Override
		public OrbisPackDataShapeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			UUID uniqueId = UUID.fromString(json.getAsJsonObject().get("unique_id").getAsString());
			String name = json.getAsJsonObject().get("name").getAsString();
			String inequalityMathExpression = json.getAsJsonObject().get("inequality_math_expression").getAsString();
			String iconLocation = json.getAsJsonObject().get("icon_location").getAsString();

			return new OrbisPackDataShapeType(uniqueId, name, inequalityMathExpression, iconLocation);
		}
	}
}
