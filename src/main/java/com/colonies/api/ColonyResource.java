package com.colonies.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Represente un lot de ressources (item + quantite) stocke dans l'entrepot de la colonie.
 * Immutable : utilisez {@link #withAmount(int)} pour modifier la quantite.
 */
public record ColonyResource(ItemStack stack, int amount) {
    public ColonyResource {
        if (stack.isEmpty()) throw new IllegalArgumentException("stack vide");
        if (amount <= 0) throw new IllegalArgumentException("quantite <= 0");
        ItemStack copy = stack.copy();
        copy.setCount(1);
        stack = copy; // on ne stocke qu'un template d'item, la taille est portee par amount
    }

    public ColonyResource withAmount(int newAmount) {
        return newAmount <= 0 ? null : new ColonyResource(stack, newAmount);
    }

    public String key() {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", key());
        tag.putInt("Amount", amount);
        return tag;
    }

    /** Retourne null si l'entree est invalide (item inconnu / amount <= 0). */
    public static ColonyResource fromTag(CompoundTag tag) {
        String id = tag.getString("Id");
        int amount = tag.getInt("Amount");
        var item = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(id));
        if (item.isEmpty() || amount <= 0) return null;
        return new ColonyResource(new ItemStack(item.get()), amount);
    }
}
