package com.colonies.registration;

import com.colonies.Colonies;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ColonyDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Colonies.MODID);

    public static final Supplier<DataComponentType<Integer>> COLONY_ID =
            DATA_COMPONENTS.register("colony_id",
                    () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
}
