package com.colonies.registration;

import com.colonies.Colonies;
import com.colonies.blockentity.ColonyCenterBlockEntity;
import com.colonies.blockentity.WorkerHutBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ColonyBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, Colonies.MODID);

    public static final Supplier<BlockEntityType<ColonyCenterBlockEntity>> COLONY_CENTER_BE =
            BLOCK_ENTITIES.register("colony_center",
                    () -> BlockEntityType.Builder.of(ColonyCenterBlockEntity::new,
                            ColonyBlocks.COLONY_CENTER.get()).build(null));

    public static final Supplier<BlockEntityType<WorkerHutBlockEntity>> WORKER_HUT_BE =
            BLOCK_ENTITIES.register("worker_hut",
                    () -> BlockEntityType.Builder.of(WorkerHutBlockEntity::new,
                            ColonyBlocks.FORESTER_HUT.get(), ColonyBlocks.MINER_HUT.get(),
                            ColonyBlocks.FARMER_HUT.get(), ColonyBlocks.SAWMILL_HUT.get(),
                            ColonyBlocks.STORES_HUT.get()).build(null));
}
