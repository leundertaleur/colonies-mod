package com.colonies.client;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/** Cache client alimente par SyncColonyPayload pour afficher les infos de colonie. */
public final class ColonyDataSync {
    private static final Map<Integer, CompoundTag> CACHE = new HashMap<>();

    private ColonyDataSync() {}

    public static void put(int id, CompoundTag tag) { CACHE.put(id, tag); }

    public static CompoundTag get(int id) { return CACHE.get(id); }
}
