package com.colonies.client;

import com.colonies.menu.ColonyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Interface de gestion de colonie : nom, ouvriers, niveau et stock. */
public class ColonyScreen extends AbstractContainerScreen<ColonyMenu> {

    public ColonyScreen(ColonyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 220;
        this.inventoryLabelVisible = false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6AC8F);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF5B4A39);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, true);
        CompoundTag colony = ColonyDataSync.get(menu.getColonyId());
        int y = 22;
        if (colony != null) {
            graphics.drawString(this.font, "Nom: " + colony.getString("Name"), 8, y, 0xFFFF55); y += 12;
            graphics.drawString(this.font, "Niveau: " + colony.getInt("Level"), 8, y, 0xFFFFFF); y += 12;
            graphics.drawString(this.font, "Ouvriers: " + colony.getInt("Workers")
                    + "/" + colony.getInt("MaxWorkers"), 8, y, 0xFFFFFF); y += 12;
            graphics.drawString(this.font, "Cabanes: " + colony.getInt("Huts"), 8, y, 0xFFFFFF); y += 16;
            graphics.drawString(this.font, "--- Stock ---", 8, y, 0xFFAAAA); y += 12;
            CompoundTag wh = colony.getCompound("Warehouse");
            int shown = 0;
            for (String key : wh.getAllKeys()) {
                if (shown++ > 10) break;
                String name = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
                graphics.drawString(this.font, name + " x" + wh.getInt(key), 8, y, 0xCCCCCC);
                y += 10;
            }
        } else {
            graphics.drawString(this.font, "Synchronisation...", 8, y, 0xFF5555);
        }
    }
}
