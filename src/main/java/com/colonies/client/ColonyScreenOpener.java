package com.colonies.client;

import com.colonies.blockentity.ColonyCenterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Ouvre le GUI de colonie cote serveur apres avoir fonde la colonie si besoin. */
public final class ColonyScreenOpener {
    private ColonyScreenOpener() {}

    public static void open(ServerPlayer player, BlockPos pos) {
        if (player.level().getBlockEntity(pos) instanceof ColonyCenterBlockEntity center) {
            int id = center.ensureColony(player);
            if (id != -1) {
                player.openMenu(center);
            }
        }
    }
}
