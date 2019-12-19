package com.gildedgames.orbis.common.dungeons;

import com.gildedgames.orbis.lib.OrbisLib;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintNetworkData;
import com.gildedgames.orbis.lib.data.management.IProject;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class DungeonViewer
{
    public DungeonViewer() {

    }

    @SubscribeEvent
    public void drawScreen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiInventory) {
            Optional<IProject> project = OrbisLib.services().getProjectManager().findProject("test");

            if (project.isPresent()) {
                Optional<UUID> id = project.get().getCache().getDataId("networktest.blueprint");

                if (id.isPresent()) {
                    Optional<BlueprintData> data = project.get().getCache().getData(id.get());

                    data.ifPresent(blueprintData -> {
                        BlueprintNetworkData network =
                                new BlueprintNetworkData(5,
                                        Lists.newArrayList(blueprintData.getMetadata().getIdentifier()),
                                        Collections.emptyList(),
                                        Collections.emptyList(),
                                        Collections.emptyList());

                        //event.setGui(new GuiBlueprintNetworkViewer(network));
                    });
                }
            }

        }
    }

}
