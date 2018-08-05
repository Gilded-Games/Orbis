package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.data.DropdownElementWithData;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdown;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTextureRepeatable;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.repeat_methods.TextureRepeatMethods;
import com.gildedgames.orbis_api.client.gui.util.repeat_methods.TextureUV;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.client.rect.RectModifier;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class GuiSelectableTree extends GuiElement
{
	private static final ResourceLocation TREE_WINDOW = OrbisCore.getResource("layer_gui/tree_window.png");

	private static final ResourceLocation TREE_WINDOW_EXTENDED = OrbisCore.getResource("layer_gui/tree_window_extended.png");

	private GuiTextureRepeatable window;

	private GuiText title;

	private List<Pair<ITextComponent, GuiTree>> managedTrees = Lists.newArrayList();

	private GuiDropdown<DropdownElementWithData<GuiTree>> dropdown;

	private GuiTree currentTree;

	public GuiSelectableTree(Rect rect)
	{
		super(rect, false);
	}

	public void setTitle(ITextComponent text)
	{
		if (this.title == null)
		{
			this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
					new Text(text, 1.0F));
		}
		else
		{
			this.title.setText(new Text(text, 1.0F));
		}
	}

	public void setTreeNoDropdown(GuiTree tree, ITextComponent text)
	{
		this.managedTrees.clear();

		this.tryRebuild();

		this.window.setResourceLocation(TREE_WINDOW,
				new TextureUV(9, 27, 182, 86), 200, 121);
		this.title.setText(new Text(text, 1.0F));

		tree.dim().mod().width(0).height(0).x(0).y(0).flush();

		tree.dim().add(new RectModifier("xFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getU(),
				RectModifier.ModifierType.X));
		tree.dim().add(new RectModifier("yFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getV(),
				RectModifier.ModifierType.Y));

		tree.dim().add(new RectModifier("widthFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getWidth(),
				RectModifier.ModifierType.WIDTH));
		tree.dim().add(new RectModifier("heightFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getHeight(),
				RectModifier.ModifierType.HEIGHT));

		tree.state().setEnabled(true);
		tree.state().setVisible(true);

		this.context().addChildren(tree);
	}

	public void setTrees(ITextComponent text, Pair<ITextComponent, GuiTree>... trees)
	{
		int selectedTreeIndex = 0;

		for (int i = 0; i < this.managedTrees.size(); i++)
		{
			Pair<ITextComponent, GuiTree> pair = this.managedTrees.get(i);

			if (pair.getRight() == this.currentTree)
			{
				selectedTreeIndex = i;
				break;
			}
		}

		if (trees.length <= selectedTreeIndex)
		{
			selectedTreeIndex = 0;
		}

		this.managedTrees.clear();

		this.managedTrees.addAll(Arrays.asList(trees));

		this.window.setResourceLocation(TREE_WINDOW_EXTENDED,
				new TextureUV(9, 47, 182, 86), 200, 141);
		this.title.setText(new Text(text, 1.0F));

		this.tryRebuild();

		this.dropdown.getList().getElements().clear();

		for (Pair<ITextComponent, GuiTree> pair : this.managedTrees)
		{
			ITextComponent t = pair.getLeft();
			GuiTree tree = pair.getRight();

			this.dropdown.getList().addDropdownElements(new DropdownElementWithData<>(t, tree));
		}

		if (this.dropdown.getList().getElements().size() >= 1)
		{
			this.dropdown.setChosenElement(this.dropdown.getList().getElements().get(selectedTreeIndex));
		}

		this.managedTrees.forEach((p) ->
		{
			GuiTree tree = p.getRight();

			tree.dim().mod().width(0).height(0).x(0).y(0).flush();

			tree.dim().add(new RectModifier("xFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getU(),
					RectModifier.ModifierType.X));
			tree.dim().add(new RectModifier("yFromWindow", this.window, (source, modifying) -> this.window.getCenterSpace().getV(),
					RectModifier.ModifierType.Y));

			tree.dim().add(new RectModifier("widthFromWindow", this.window,
					(source, modifying) -> source.dim().width() - this.window.getCenterSpace().getU() - this.window.getBottomRightCorner().getWidth(),
					RectModifier.ModifierType.WIDTH));
			tree.dim().add(new RectModifier("heightFromWindow", this.window,
					(source, modifying) -> source.dim().height() - this.window.getCenterSpace().getV() - this.window.getBottomRightCorner().getHeight(),
					RectModifier.ModifierType.HEIGHT));

			tree.state().setEnabled(false);
			tree.state().setVisible(false);

			this.context().addChildren(p.getRight());
		});

		if (!this.managedTrees.isEmpty())
		{
			this.currentTree = this.managedTrees.get(selectedTreeIndex).getRight();

			this.currentTree.state().setEnabled(true);
			this.currentTree.state().setVisible(true);
		}
	}

	@Override
	public void build()
	{
		if (this.window == null)
		{
			this.window = new GuiTextureRepeatable(Dim2D.build().flush(), TREE_WINDOW,
					new TextureUV(9, 27, 182, 86), 200, 121, TextureRepeatMethods.UNIFORM_EDGES);
		}

		if (!this.window.dim().containsModifier("usingWindowArea"))
		{
			this.window.dim().add(new RectModifier("usingWindowArea", this, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
		}

		if (this.title == null)
		{
			this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
					new Text(new TextComponentTranslation("orbis.gui.conditions", ""), 1.0F));
		}

		this.context().addChildren(this.window, this.title);

		if (this.dropdown == null)
		{
			this.dropdown = new GuiDropdown<>(Dim2D.build().x(7).y(25).width(186).flush(), (e) -> {
				if (this.currentTree != null)
				{
					this.currentTree.state().setEnabled(false);
					this.currentTree.state().setVisible(false);
				}

				this.currentTree = e.getData();

				this.currentTree.state().setEnabled(true);
				this.currentTree.state().setVisible(true);
			});
		}

		if (this.managedTrees.size() >= 2)
		{
			this.context().addChildren(this.dropdown);

			this.dropdown.state().setZOrder(Integer.MAX_VALUE);
		}
	}
}