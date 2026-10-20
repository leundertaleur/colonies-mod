package com.colonies.menu;

import com.colonies.colony.ColonyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;

/** Menu de l'interface de gestion de colonie (cote serveur + client). */
public class ColonyMenu extends AbstractContainerMenu {
    private final int colonyId;

    // Constructeur client (depuis le buffer envoye par le serveur)
    public ColonyMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, buf == null ? -1 : buf.readInt());
    }

    // Constructeur serveur
    public ColonyMenu(int containerId, Inventory inventory, int colonyId) {
        super(com.colonies.registration.ColonyMenus.MENU_TYPE.get(), containerId);
        this.colonyId = colonyId;
    }

    /** Type de menu NeoForge (factory client avec buffer). */
    public static MenuType<ColonyMenu> createMenuType() {
        return IMenuTypeExtension.create(
                (IContainerFactory<ColonyMenu>) (windowId, inv, data) -> new ColonyMenu(windowId, inv, data));
    }

    public int getColonyId() { return colonyId; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Pas d'inventaire de colonie dans cette version : les slots restent vides.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // On reste dans le menu tant que la colonie existe.
        return ColonyManager.getInstance().getById(colonyId).isPresent();
    }
}
