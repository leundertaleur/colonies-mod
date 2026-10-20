package com.colonies.blockentity;

import com.colonies.Colonies;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Detecte la destruction des cabanes pour nettoyer les donnees de colonie. */
@EventBusSubscriber(modid = Colonies.MODID)
public class BlockEventsHandler {

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
            BlockEntity be = level.getBlockEntity(event.getPos());
            if (be instanceof WorkerHutBlockEntity hut) {
                hut.onRemove(level);
            }
        }
    }
}
