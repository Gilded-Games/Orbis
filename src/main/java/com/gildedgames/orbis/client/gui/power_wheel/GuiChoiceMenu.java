package com.gildedgames.orbis.client.gui.power_wheel;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiElement;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.util.InputHelper;
import com.hrznstudio.roadworks.api.RoadworksAPI;
import com.hrznstudio.roadworks.api.input.Controller;
import com.hrznstudio.roadworks.api.input.ControllerManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

public class GuiChoiceMenu extends GuiElement
{

	private final static ResourceLocation GRADIENT_TEXTURE = OrbisCore.getResource("godmode/overlay/choose_power_gradient.png");

	private final static ResourceLocation BACKDROP_TEXTURE = OrbisCore.getResource("godmode/overlay/choose_power_backdrop.png");

	private final static ResourceLocation ARROW_TEXTURE = OrbisCore.getResource("godmode/overlay/choose_power_arrow.png");

	private final static ResourceLocation CURSOR_TEXTURE = OrbisCore.getResource("godmode/overlay/choose_power_cursor.png");

	private static boolean LAST_MOUSE_SET = false;

	private static int LAST_MOUSE_X, LAST_MOUSE_Y;

	private final float choiceRadius = 35;

	protected Choice[] choices;

	private GuiTexture arrow;

	private GuiText choiceName;

	private Choice hoveredChoice;

	public GuiChoiceMenu(final Choice... choices)
	{
		super(Dim2D.flush(), true);

		this.choices = choices;
	}

	public GuiText getChoiceName()
	{
		return this.choiceName;
	}

	public Choice getHoveredChoice()
	{
		return this.hoveredChoice;
	}

	@Override
	public void onGuiClosed(GuiElement element)
	{
		GuiChoiceMenu.LAST_MOUSE_X = Mouse.getEventX();
		GuiChoiceMenu.LAST_MOUSE_Y = Mouse.getEventY();
	}

	@Override
	public void build()
	{
		final Pos2D center = InputHelper.getCenter();

		if (!GuiChoiceMenu.LAST_MOUSE_SET)
		{
			GuiChoiceMenu.LAST_MOUSE_X = Display.getWidth() / 2;
			GuiChoiceMenu.LAST_MOUSE_Y = Display.getHeight() / 2;

			GuiChoiceMenu.LAST_MOUSE_SET = true;
		}

		Mouse.setCursorPosition(GuiChoiceMenu.LAST_MOUSE_X, GuiChoiceMenu.LAST_MOUSE_Y);

		final GuiTexture gradient = new GuiTexture(Dim2D.build().pos(center).center(true).width(125).height(125).flush(), GRADIENT_TEXTURE);
		final GuiTexture backdrop = new GuiTexture(Dim2D.build().pos(center).addX(-1).center(true).width(44).height(44).flush(), BACKDROP_TEXTURE);

		this.arrow = new GuiTexture(Dim2D.build().pos(center).addX(-0.5F).addY(-2).center(true).width(11).height(12).flush(), ARROW_TEXTURE);
		//this.arrow.dim().mod().origin(0, 3).flush();

		this.choiceName = new GuiText(Dim2D.build().center(true).pos(center).addY(-86).flush(), null);

		this.context().addChildren(gradient);
		this.context().addChildren(backdrop);
		this.context().addChildren(this.arrow);

		final float powerAngleStep = (float) Math.toDegrees((2 * Math.PI) / this.choices.length);

		float angle = -90;

		for (final Choice choice : this.choices)
		{
			final GuiTexture icon = choice.getIcon();

			final float x = this.getChoiceX(icon, angle, this.choiceRadius) + center.x();
			final float y = this.getChoiceY(icon, angle, this.choiceRadius) + center.y();

			icon.dim().mod().center(true).x(x).y(y).flush();

			this.context().addChildren(icon);

			angle += powerAngleStep;
		}

		this.context().addChildren(this.choiceName);
	}

	@Override
	public void onDraw(GuiElement element)
	{
		final Pos2D center = Pos2D.flush(this.viewer().getScreenWidth() / 2, this.viewer().getScreenHeight() / 2);


		float dx = center.x() - InputHelper.getMouseX();
		float dy = center.y() - InputHelper.getMouseY();

		Controller controller = null;
		if (RoadworksAPI.isAvailable())
		{
			ControllerManager manager = RoadworksAPI.getInstance().getControllerManager();
			if (manager.getActiveController().isPresent())
			{
				controller = manager.getActiveController().get();
				if (controller.isConnected())
				{
					dx = 0 - controller.getAxis(Controller.Axis.X, Controller.Stick.LEFT);
					dy = controller.getAxis(Controller.Axis.Y, Controller.Stick.LEFT);
				}
			}
		}

		final float degrees = (float) (Math.toDegrees(Math.atan2(dy, dx))) - 90;

		this.arrow.dim().mod().degrees(degrees).flush();

		Vector2f cursor = new Vector2f(dx, dy);
		cursor.scale(30);
		cursor.negate();
		final float distance;
		if (controller != null)
		{
			distance = (float) Math
					.sqrt((cursor.x) * (cursor.x) + (cursor.y) * (cursor.y));
		} else
		{
			distance = (float) Math
					.sqrt((center.x() - InputHelper.getMouseX()) * (center.x() - InputHelper.getMouseX()) + (center.y() - InputHelper.getMouseY()) * (center.y()
							- InputHelper.getMouseY()));
		}

		double closestDist = Double.MAX_VALUE;

		Choice closestChoice = null;

		for (final Choice choice : this.choices)
		{
			final GuiTexture icon = choice.getIcon();
			if (controller != null)
			{

				final double choiceDist = Math.sqrt(
						(choice.getIcon().dim().centerX() - (center.x() + cursor.x)) *
								(choice.getIcon().dim().centerX() - (center.x() + cursor.x)) +
								(choice.getIcon().dim().centerY() - (center.y() + cursor.y)) *
										(choice.getIcon().dim().centerY() - (center.y() + cursor.y)));

				if (choiceDist < closestDist)
				{
					closestDist = choiceDist;
					closestChoice = choice;
				}
			} else
			{
				final double choiceDist = Math.sqrt((icon.dim().centerX() - InputHelper.getMouseX()) * (icon.dim().centerX() - InputHelper.getMouseX())
						+ (icon.dim().centerY() - InputHelper.getMouseY()) * (icon.dim().centerY() - InputHelper.getMouseY()));

				if (choiceDist < closestDist)
				{
					closestDist = choiceDist;
					closestChoice = choice;
				}
			}
		}

		this.hoveredChoice = closestChoice;

		final float NORMAL_SCALE = 1F;
		final float SELECTED_SCALE = 1.1F;

		if (distance > this.choiceRadius + 10 && this.hoveredChoice != null)
		{
			final GuiTexture icon = this.hoveredChoice.getIcon();

			icon.dim().mod().scale(NORMAL_SCALE).flush();

			this.choiceName.setText(null);

			this.hoveredChoice = null;
		} else
		{
			this.choiceName.setText(new Text(new TextComponentString(this.hoveredChoice.name()), 1.0F));

			if (controller != null)
			{
				if (controller.isDown(Controller.Button.A))
				{
					closestChoice.onSelect(PlayerOrbis.get(this.viewer().mc().player));
				}
			} else
			{
				if (Mouse.isButtonDown(0))
				{
					closestChoice.onSelect(PlayerOrbis.get(this.viewer().mc().player));
				}
			}

			for (final Choice choice : this.choices)
			{
				final GuiTexture icon = choice.getIcon();

				if (choice == closestChoice)
				{
					icon.dim().mod().scale(SELECTED_SCALE).flush();
				} else
				{
					icon.dim().mod().scale(NORMAL_SCALE).flush();
				}
			}
		}
	}

	private float getChoiceX(final IGuiElement gui, final float degreesAngle, final float radius)
	{
		return Math.round(radius * Math.cos(Math.toRadians(degreesAngle)));
	}

	private float getChoiceY(final IGuiElement gui, final float degreesAngle, final float radius)
	{
		return Math.round(radius * Math.sin(Math.toRadians(degreesAngle)));
	}

	public interface Choice
	{

		void onSelect(PlayerOrbis playerOrbis);

		GuiTexture getIcon();

		String name();

	}
}