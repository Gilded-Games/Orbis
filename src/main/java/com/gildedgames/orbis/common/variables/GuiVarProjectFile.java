package com.gildedgames.orbis.common.variables;

import com.gildedgames.orbis.common.variables.displays.GuiProjectFileChooser;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElementWithData;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.core.variables.IGuiVar;
import com.gildedgames.orbis.lib.core.variables.IGuiVarCompareExpression;
import com.gildedgames.orbis.lib.core.variables.IGuiVarMutateExpression;
import com.gildedgames.orbis.lib.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiVarProjectFile implements IGuiVar<IDataIdentifier, GuiProjectFileChooser>
{
	public static final List<DropdownElementWithData<Supplier<IGuiVarCompareExpression>>> COMPARE_EXPRESSIONS = Lists.newArrayList();

	public static final List<DropdownElementWithData<Supplier<IGuiVarMutateExpression>>> MUTATE_EXPRESSIONS = Lists.newArrayList();

	private IDataIdentifier data;

	private String name = "";

	private Function<String, Boolean> extensionValidator;

	private GuiVarDisplay parentDisplay;

	private Function<IData, Boolean> onDoubleClickFile = (data ->
	{
		this.data = data.getMetadata().getIdentifier();

		this.parentDisplay.refresh();

		return this.data != null;
	});

	private GuiVarProjectFile()
	{

	}

	public GuiVarProjectFile(String name, Function<String, Boolean> extensionValidator)
	{
		this.name = name;
		this.extensionValidator = extensionValidator;
	}

	public Function<String, Boolean> getExtensionValidator()
	{
		return this.extensionValidator;
	}

	public void setExtensionValidator(Function<String, Boolean> extensionValidator)
	{
		this.extensionValidator = extensionValidator;
	}

	@Override
	public void setParentDisplay(GuiVarDisplay parentDisplay)
	{
		this.parentDisplay = parentDisplay;
	}

	@Override
	public String getVariableName()
	{
		return this.name;
	}

	@Override
	public String getDataName()
	{
		return "orbis.gui.itemstack";
	}

	@Override
	public IDataIdentifier getData()
	{
		return this.data;
	}

	@Override
	public void setData(IDataIdentifier data)
	{
		this.data = data;
	}

	@Override
	public GuiProjectFileChooser createDisplay(int maxWidth)
	{
		return new GuiProjectFileChooser(Dim2D.build().width(maxWidth).x(0).height(40).flush(), this, this.extensionValidator, this.onDoubleClickFile);
	}

	@Override
	public void updateDataFromDisplay(GuiProjectFileChooser guiFrame)
	{
		this.data = guiFrame.getChosenDataIdentifier();
	}

	@Override
	public void resetDisplayFromData(GuiProjectFileChooser guiFrame)
	{
		guiFrame.setChosenDataIdentifier(this.data);
	}

	@Override
	public List<DropdownElementWithData<Supplier<IGuiVarCompareExpression>>> getCompareExpressions()
	{
		return COMPARE_EXPRESSIONS;
	}

	@Override
	public List<DropdownElementWithData<Supplier<IGuiVarMutateExpression>>> getMutateExpressions()
	{
		return MUTATE_EXPRESSIONS;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("data", this.data);
		tag.setString("name", this.name);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.data = funnel.get("data");
		this.name = tag.getString("name");
	}
}