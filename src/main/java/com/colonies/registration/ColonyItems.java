package com.colonies.registration;

import com.colonies.Colonies;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Colonies.MODID);

    public static final DeferredItem<BlockItem> COLONY_CENTER =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.COLONY_CENTER);
    public static final DeferredItem<BlockItem> FORESTER_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.FORESTER_HUT);
    public static final DeferredItem<BlockItem> MINER_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.MINER_HUT);
    public static final DeferredItem<BlockItem> FARMER_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.FARMER_HUT);
    public static final DeferredItem<BlockItem> SAWMILL_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.SAWMILL_HUT);
    public static final DeferredItem<BlockItem> STORES_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.STORES_HUT);
    public static final DeferredItem<BlockItem> KNIGHT_HUT =
            ITEMS.registerSimpleBlockItem(ColonyBlocks.KNIGHT_HUT);

    // Outil pour ouvrir l'interface de gestion de colonie.
    public static final DeferredItem<Item> COLONY_TOOL = ITEMS.register("colony_tool",
            () -> new Item(new Item.Properties().stacksTo(1)));
}
