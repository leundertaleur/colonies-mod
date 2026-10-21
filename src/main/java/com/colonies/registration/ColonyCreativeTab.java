package com.colonies.registration;

import com.colonies.Colonies;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ColonyCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Colonies.MODID);

    public static final Supplier<CreativeModeTab> COLONY_TAB = CREATIVE_MODE_TABS.register("colony_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.colonies"))
                    .icon(() -> new ItemStack(ColonyItems.COLONY_CENTER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ColonyItems.COLONY_CENTER.get());
                        output.accept(ColonyItems.FORESTER_HUT.get());
                        output.accept(ColonyItems.MINER_HUT.get());
                        output.accept(ColonyItems.FARMER_HUT.get());
                        output.accept(ColonyItems.SAWMILL_HUT.get());
                        output.accept(ColonyItems.STORES_HUT.get());
                        output.accept(ColonyItems.KNIGHT_HUT.get());
                        output.accept(ColonyItems.COLONY_TOOL.get());
                    })
                    .build());
}
