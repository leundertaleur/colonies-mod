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

    /** Le metier est exerce par un Chevalier (entite dediee) et non par un Colon standard. */
    public boolean isKnight() { return this == KNIGHT; }

    /** Ce que le colon consomme comme nourriture (1 unite par jour de colonie). */
    public Item getFoodItem() { return Items.BREAD; }

    /** Salaire en emeraudes par jour de colonie. */
    public int getWage() {
        return switch (this) {
            case KNIGHT -> 2;
            case MINER -> 2;
            default -> 1;
        };
    }

    public ItemStack getIcon() { return new ItemStack(this.iconSupplier.get()); }

    public static ColonyJob fromId(String id) {
        for (ColonyJob job : values()) if (job.id.equals(id)) return job;
        return null;
    }
}
