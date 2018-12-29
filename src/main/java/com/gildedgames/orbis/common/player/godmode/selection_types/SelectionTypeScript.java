package com.gildedgames.orbis.common.player.godmode.selection_types;

import com.gildedgames.orbis.client.ISelectionTypeClient;
import com.gildedgames.orbis.client.godmode.selection_types.SelectionTypeScriptClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.data.ScriptedShape;
import com.gildedgames.orbis.player.IPlayerOrbis;
import com.gildedgames.orbis.player.designer_mode.ISelectionType;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.data.region.IShape;
import net.benmann.evald.Evald;
import net.benmann.evald.Library;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

public class SelectionTypeScript implements ISelectionType
{
	private ISelectionTypeClient client;

	private String name, inequalityMathExpression;

	private ResourceLocation iconLocation;

	private UUID uniqueId;

	private int aX, aY, aZ;

	private Evald evald;

	public SelectionTypeScript(UUID uniqueId, String name, String inequalityMathExpression, ResourceLocation iconLocation)
	{
		this.uniqueId = uniqueId;
		this.name = name;
		this.inequalityMathExpression = inequalityMathExpression;
		this.iconLocation = iconLocation;

		this.evald = new Evald();

		this.evald.addLibrary(Library.ALL);

		this.aX = this.evald.addVariable("x");
		this.aY = this.evald.addVariable("y");
		this.aZ = this.evald.addVariable("z");

		this.evald.parse(this.inequalityMathExpression);
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.uniqueId);

		return builder.toHashCode();
	}

	@Override
	public ISelectionTypeClient getClient()
	{
		if (OrbisCore.isClient() && this.client == null)
		{
			GuiTexture icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), this.iconLocation);

			this.client = new SelectionTypeScriptClient(this.name, icon);
		}

		return this.client;
	}

	@Override
	public IShape createShape(BlockPos start, BlockPos end, IPlayerOrbis playerOrbis, boolean centered)
	{
		return new ScriptedShape(start, end, centered, this.inequalityMathExpression, this.aX, this.aY, this.aZ, this.evald);
	}

	@Override
	public void write(NBTTagCompound tag)
	{

	}

	@Override
	public void read(NBTTagCompound tag)
	{

	}
}