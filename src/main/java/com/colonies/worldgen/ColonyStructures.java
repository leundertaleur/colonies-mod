package com.colonies.worldgen;

import com.colonies.Colonies;
import net.neoforged.bus.api.IEventBus;

/**
 * Point d'entree pour les structures/decors futurs (ruines de colonie, etc.).
 * En attente : registries STRUCTURE / TEMPLATE via DeferredRegister si besoin.
 */
public class ColonyStructures {
    public static void register(IEventBus bus) {
        // Reserve pour des generations de structures de colonies abandonnees.
    }
}
