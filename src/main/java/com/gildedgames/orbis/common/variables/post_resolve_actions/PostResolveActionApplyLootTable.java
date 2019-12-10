package com.gildedgames.orbis.common.variables.post_resolve_actions;

import com.gildedgames.orbis.common.variables.GuiVarProjectFile;
import com.gildedgames.orbis.lib.client.rect.Pos2D;
import com.gildedgames.orbis.lib.core.baking.BakedLootTableApply;
import com.gildedgames.orbis.lib.core.baking.IBakedPosAction;
import com.gildedgames.orbis.lib.core.variables.IGuiVar;
import com.gildedgames.orbis.lib.core.variables.displays.GuiVarDisplay;
import com.gildedgames.orbis.lib.core.variables.post_resolve_actions.IPostResolveAction;
import com.gildedgames.orbis.lib.data.IDataUser;
import com.gildedgames.orbis.lib.data.json.JsonData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.data.schedules.IPosActionBaker;
import com.gildedgames.orbis.lib.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class PostResolveActionApplyLootTable implements IPostResolveAction, IDataUser<ScheduleRegion>, IPosActionBaker
{
	private static final Function<String, Boolean> JSON_VALIDATOR = (extension -> extension.equals(JsonData.EXTENSION));

	private List<IGuiVar> variables = Lists.newArrayList();

	private GuiVarProjectFile lootTableChooser;

	private Pos2D guiPos = Pos2D.ORIGIN;

	private GuiVarDisplay parentDisplay;

	private ScheduleRegion scheduleRegion;

	public PostResolveActionApplyLootTable()
	{
		this.lootTableChooser = new GuiVarProjectFile("orbis.gui.loot_table", JSON_VALIDATOR);

		this.variables.add(this.lootTableChooser);
	}

	@Override
	public String getName()
	{
		return "orbis.gui.apply_loot_table";
	}

	@Override
	public List<IGuiVar> getVariables()
	{
		return this.variables;
	}

	@Override
	public void resolve(Random rand)
	{

	}

	@Override
	public Pos2D getGuiPos()
	{
		return this.guiPos;
	}

	@Override
	public void setGuiPos(Pos2D pos)
	{
		this.guiPos = pos;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("lootTableChooser", this.lootTableChooser);
		funnel.set("guiPos", this.guiPos, NBTFunnel.POS2D_SETTER);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.lootTableChooser = funnel.get("lootTableChooser");
		this.guiPos = funnel.getWithDefault("guiPos", NBTFunnel.POS2D_GETTER, () -> this.guiPos);

		this.lootTableChooser.setExtensionValidator(JSON_VALIDATOR);

		this.variables.clear();

		this.variables.add(this.lootTableChooser);
	}

	@Override
	public void setParentDisplay(GuiVarDisplay parentDisplay)
	{
		this.parentDisplay = parentDisplay;
	}

	@Override
	public String getDataIdentifier()
	{
		return "scheduleRegion";
	}

	@Override
	public void setUsedData(ScheduleRegion scheduleRegion)
	{
		this.scheduleRegion = scheduleRegion;
	}

	@Override
	public List<IBakedPosAction> bakeActions(IRegion bounds, Random rand, Rotation rotation)
	{
		if (this.lootTableChooser.getData() == null)
		{
			return Collections.emptyList();
		}

		List<IBakedPosAction> actions = Lists.newArrayList();

		for (BlockPos.MutableBlockPos pos : bounds.getShapeData())
		{
			BakedLootTableApply action = new BakedLootTableApply(this.lootTableChooser.getData(), rand.nextLong(), pos.toImmutable());

			actions.add(action);
		}

		return actions;
	}
}
