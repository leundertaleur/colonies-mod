package com.colonies.registration;

import com.colonies.Colonies;
import com.colonies.block.ColonyCenterBlock;
import com.colonies.block.WorkerHutBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Colonies.MODID);

    // Le centre de colonie : bloc principal permettant de fonder une colonie.
    public static final DeferredBlock<ColonyCenterBlock> COLONY_CENTER = BLOCKS.register("colony_center",
            () -> new ColonyCenterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(4.0f).sound(SoundType.METAL)));

    // Les cabanes d'ouvriers (huts) - chaque type donne un metier a un colon.
    public static final DeferredBlock<WorkerHutBlock> FORESTER_HUT = BLOCKS.register("forester_hut",
            () -> new WorkerHutBlock(com.colonies.api.ColonyJob.FORESTER, props(DyeColor.GREEN)));
    public static final DeferredBlock<WorkerHutBlock> MINER_HUT = BLOCKS.register("miner_hut",
            () -> new WorkerHutBlock(com.colonies.api.ColonyJob.MINER, props(DyeColor.GRAY)));
    public static final DeferredBlock<WorkerHutBlock> FARMER_HUT = BLOCKS.register("farmer_hut",
            () -> new WorkerHutBlock(com.colonies.api.ColonyJob.FARMER, props(DyeColor.YELLOW)));
    public static final DeferredBlock<WorkerHutBlock> SAWMILL_HUT = BLOCKS.register("sawmill_hut",
            () -> new WorkerHutBlock(com.colonies.api.ColonyJob.SAWMILL_WORKER, props(DyeColor.BROWN)));
    public static final DeferredBlock<WorkerHutBlock> STORES_HUT = BLOCKS.register("stores_hut",
            () -> new WorkerHutBlock(null, props(DyeColor.BLUE)));

    // La caserne : recrute un Chevalier qui defend la colonie.
    public static final DeferredBlock<WorkerHutBlock> KNIGHT_HUT = BLOCKS.register("knight_hut",
            () -> new WorkerHutBlock(com.colonies.api.ColonyJob.KNIGHT, props(DyeColor.RED)));

    private static BlockBehaviour.Properties props(DyeColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color.getMapColor())
                .strength(3.0f).sound(SoundType.WOOD);
    }
}
