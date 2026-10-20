package com.colonies.client;

import com.colonies.Colonies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Setup client : enregistrement de l'ecran du menu de colonie.
 * Sur NeoForge 1.21.1 (MC 1.21), MenuScreens.register(...) est prive :
 * la methode officielle est l'evenement RegisterMenuScreensEvent.
 */
@EventBusSubscriber(modid = Colonies.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(com.colonies.registration.ColonyMenus.MENU_TYPE.get(), ColonyScreen::new);
    }
}
