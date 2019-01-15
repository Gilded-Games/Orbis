package com.gildedgames.orbis.common.compat;

import com.gildedgames.orbis.client.gui.fill.GuiCombineMatrix;
import com.gildedgames.orbis_api.client.gui.util.GuiAbstractButton;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;

@JEIPlugin
public class OrbisJEIPlugin implements IModPlugin
{
    @Override
    public void register(IModRegistry registry)
    {
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiCombineMatrix>()
        {
            @Override
            public Class<GuiCombineMatrix> getGuiContainerClass()
            {
                return GuiCombineMatrix.class;
            }

            @Nullable
            @Override
            public List<Rectangle> getGuiExtraAreas(GuiCombineMatrix guiContainer)
            {
                GuiTexture matrix = guiContainer.getMatrix();
                GuiTexture flow = guiContainer.getFlow();
                GuiAbstractButton forge = guiContainer.getForgeButton();
                return Collections.singletonList(
                        new Rectangle(
                                (int) matrix.state().dim().x(),
                                (int) matrix.state().dim().y(),
                                (int) matrix.state().dim().width(),
                                (int) (
                                        matrix.state().dim().height() +
                                                flow.state().dim().height() +
                                                forge.state().dim().height()
                                )
                        )
                );
            }
        });
    }
}