package com.colonies.blockentity;

import com.colonies.Colonies;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Detecte la destruction des cabanes pour nettoyer les donnees de colonie. */
@EventBusSubscriber(modid = Colonies.MODID)
public class BlockEventsHandler {

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        // Le nettoyage est deja fait proprement dans WorkerHutBlock#onRemove (vanilla hook).
        // Ce handler n'est garde que comme filet de securite si le block entity est orphelin.
        if (event.getLevel() instanceof Level level
                && level.getBlockEntity(event.getPos()) instanceof WorkerHutBlockEntity hut) {
            hut.onRemove(level);
        }
    }
}
