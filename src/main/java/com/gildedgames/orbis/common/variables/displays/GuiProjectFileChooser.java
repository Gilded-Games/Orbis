package com.gildedgames.orbis.common.variables.displays;

import com.gildedgames.orbis.common.variables.GuiVarProjectFile;
import com.gildedgames.orbis_api.OrbisLib;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.client.rect.RectModifier;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;

import java.util.function.Function;

public class GuiProjectFileChooser extends GuiElement
{
	private GuiVarProjectFile varProjectFile;

	private IDataIdentifier dataIdentifier;

	private GuiButtonVanilla button;

	private GuiText dataIdentifierText;

	private Function<String, Boolean> extensionValidator;

	private Function<IData, Boolean> onDoubleClickFile;

	public GuiProjectFileChooser(Rect rect, GuiVarProjectFile varProjectfile, Function<String, Boolean> extensionValidator,
			Function<IData, Boolean> onDoubleClickFile)
	{
		super(rect, true);

		this.varProjectFile = varProjectfile;

		this.dataIdentifier = varProjectfile.getData();

		this.extensionValidator = extensionValidator;
		this.onDoubleClickFile = onDoubleClickFile;
	}

	public void setExtensionValidator(Function<String, Boolean> extensionValidator)
	{
		this.extensionValidator = extensionValidator;
	}

	public void setOnDoubleClickFile(Function<IData, Boolean> onDoubleClickFile)
	{
		this.onDoubleClickFile = onDoubleClickFile;
	}

	public GuiVarProjectFile getVarProjectFile()
	{
		return this.varProjectFile;
	}

	public IDataIdentifier getChosenDataIdentifier()
	{
		return this.dataIdentifier;
	}

	public void setChosenDataIdentifier(IDataIdentifier dataIdentifier)
	{
		this.dataIdentifier = dataIdentifier;

		this.dataIdentifierText.setText(new Text(new TextComponentString(
				"Chosen: " + (this.dataIdentifier == null ? "None" : OrbisLib.services().getProjectManager().findMetadata(this.dataIdentifier))),
				1.0F));
	}

	@Override
	public void build()
	{
		this.button = new GuiButtonVanilla(Dim2D.build().height(20).flush());

		this.button.getInner().displayString = I18n.format("orbis.gui.choose_project_file");

		if (!this.button.dim().containsModifier("width"))
		{
			this.button.dim().add(new RectModifier("width", this, RectModifier.ModifierType.WIDTH.getModification(), RectModifier.ModifierType.WIDTH));
		}

		this.dataIdentifierText = new GuiText(Dim2D.build().y(30).flush(),
				new Text(new TextComponentString(
						"Chosen: " + (this.dataIdentifier == null ? "None" : OrbisLib.services().getProjectManager().findMetadata(this.dataIdentifier))),
						1.0F));

		this.context().addChildren(this.button, this.dataIdentifierText);

		this.button.state().setCanBeTopHoverElement(true);
	}

	@Override
	public void onMouseClicked(GuiElement element, final int mouseX, final int mouseY, final int mouseButton)
	{
		if (this.button.state().isHoveredAndTopElement() && mouseButton == 0 && this.viewer().mc().currentScreen instanceof GuiViewer)
		{
			this.viewer().mc()
					.displayGuiScreen(new GuiViewProjects((GuiViewer) this.viewer().mc().currentScreen, this.extensionValidator, this.onDoubleClickFile));
		}
	}
}
