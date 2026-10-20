package com.colonies.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

/** Les metiers disponibles dans les colonies (inspire de MineColonies). */
public enum ColonyJob {
    FORESTER("forester", () -> Items.OAK_SAPLING),
    MINER("miner", () -> Items.IRON_PICKAXE),
    FARMER("farmer", () -> Items.WHEAT),
    SAWMILL_WORKER("sawmill_worker", () -> Items.OAK_PLANKS),
    KNIGHT("knight", () -> Items.STONE_SWORD);

    private final String id;
    private final Supplier<Item> iconSupplier;

    ColonyJob(String id, Supplier<Item> iconSupplier) {
        this.id = id;
        this.iconSupplier = iconSupplier;
    }

    public String getId() { return id; }

    public ItemStack getIcon() { return new ItemStack(this.iconSupplier.get()); }

    public static ColonyJob fromId(String id) {
        for (ColonyJob job : values()) if (job.id.equals(id)) return job;
        return null;
    }
}
