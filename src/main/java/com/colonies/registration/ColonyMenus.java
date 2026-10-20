package com.colonies.registration;

import com.colonies.Colonies;
import com.colonies.menu.ColonyMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ColonyMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Colonies.MODID);

    public static final Supplier<MenuType<ColonyMenu>> MENU_TYPE = MENUS.register("colony_menu",
            ColonyMenu::createMenuType);
}
