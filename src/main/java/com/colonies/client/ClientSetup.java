package com.colonies.client;

import com.colonies.Colonies;
import com.colonies.menu.ColonyMenu;
import com.colonies.registration.ColonyMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Setup client : enregistrement de l'ecran du menu de colonie. */
@EventBusSubscriber(modid = Colonies.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ColonyMenus.MENU_TYPE.get(), ColonyMenu::createScreen));
    }
}
