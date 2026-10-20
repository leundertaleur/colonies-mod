package com.colonies.client;

import com.colonies.Colonies;
import com.colonies.registration.ColonyMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Setup client : enregistrement de l'ecran du menu de colonie.
 * Sur NeoForge 1.21.1 (MC 1.21), MenuScreens.register(...) est privé :
 * la methode officielle est l'evennement RegisterMenuScreensEvent.
 */
@EventBusSubscriber(modid = Colonies.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ColonyMenus.MENU_TYPE.get(), ColonyScreen::new);
    }
}
