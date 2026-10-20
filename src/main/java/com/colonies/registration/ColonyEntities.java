package com.colonies.registration;

import com.colonies.Colonies;
import com.colonies.colony.Colonist;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ColonyEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Colonies.MODID);

    // Le colon : PNJ qui travaille dans la colonie.
    public static final Supplier<EntityType<Colonist>> COLONIST = ENTITY_TYPES.register("colonist",
            () -> EntityType.Builder.of(Colonist::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build("colonist"));
}
