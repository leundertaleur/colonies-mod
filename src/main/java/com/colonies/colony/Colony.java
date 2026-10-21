package com.colonies.colony;

import com.colonies.api.ColonyJob;
import com.colonies.api.ColonyResource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Represente une colonie : territoire, nom, cabanes, ouvriers, stock, nourriture et salaires. */
public class Colony {
    /** Duree d'une "journee de colonie" en ticks serveur (10 min reelles). */
    public static final int COLONY_DAY_TICKS = 12000;

    private final int id;
    private String name = "Colonie";
    private UUID ownerUUID;
    private BlockPos centerPos = BlockPos.ZERO;
    private int radius = 20;
    private int level = 1;
    /** Compteur temps pour la journee de colonie (nourriture/salaires). */
    private long dayTimer = 0;

    /** Positions des cabanes d'ouvriers -> metier associe. */
    private final Map<BlockPos, ColonyJob> huts = new HashMap<>();
    /** Colons actifs (uuid). */
    private final List<UUID> workers = new ArrayList<>();
    /** Stock de la colonie sous forme de lots (item + quantite), alimente par la production. */
    private final Map<String, ColonyResource> warehouse = new LinkedHashMap<>();

    public Colony(int id) { this.id = id; }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwner() { return ownerUUID; }
    public void setOwner(UUID owner) { this.ownerUUID = owner; }
    public BlockPos getCenterPos() { return centerPos; }
    public void setCenterPos(BlockPos pos) { this.centerPos = pos.immutable(); }
    public int getRadius() { return radius; }
    public int getLevel() { return level; }

    public boolean isInside(BlockPos pos) {
        return Math.abs(pos.getX() - centerPos.getX()) <= radius
                && Math.abs(pos.getZ() - centerPos.getZ()) <= radius;
    }

    public void addHut(BlockPos pos, ColonyJob job) { if (job != null) huts.put(pos.immutable(), job); }
    public void removeHut(BlockPos pos) { huts.remove(pos); }
    public Map<BlockPos, ColonyJob> getHuts() { return huts; }

    public void addWorker(UUID uuid) { if (!workers.contains(uuid)) workers.add(uuid); }
    public void removeWorker(UUID uuid) { workers.remove(uuid); }
    public List<UUID> getWorkers() { return workers; }
    public int getWorkerCount() { return workers.size(); }

    // ---------- Entrepot ----------
    public void addToStock(ItemStack stack) {
        if (stack.isEmpty()) return;
        ColonyResource res = new ColonyResource(stack, stack.getCount());
        warehouse.merge(res.key(), res, (a, b) -> a.withAmount(a.amount() + b.amount()));
    }

    public void addToStock(String key, int amount) {
        if (amount <= 0) return;
        var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getOptional(net.minecraft.resources.ResourceLocation.parse(key));
        if (itemOpt.isEmpty()) return;
        ColonyResource res = new ColonyResource(new ItemStack(itemOpt.get()), amount);
        warehouse.merge(key, res, (a, b) -> a.withAmount(a.amount() + b.amount()));
    }

    /** Retire jusqu'a `amount` units de l'item ; retourne le nombre reellement retire. */
    public int removeFromStock(String key, int amount) {
        ColonyResource res = warehouse.get(key);
        if (res == null || amount <= 0) return 0;
        int removed = Math.min(res.amount(), amount);
        ColonyResource remaining = res.withAmount(res.amount() - removed);
        if (remaining == null) warehouse.remove(key); else warehouse.put(key, remaining);
        return removed;
    }

    public int getStock(String key) {
        ColonyResource res = warehouse.get(key);
        return res == null ? 0 : res.amount();
    }

    public Map<String, ColonyResource> getWarehouseResources() { return warehouse; }

    /** Legacy : vue simple cle -> quantite (utilisée par l'UI synchronisee). */
    public Map<String, Integer> getWarehouse() {
        Map<String, Integer> view = new LinkedHashMap<>();
        warehouse.forEach((k, r) -> view.put(k, r.amount()));
        return view;
    }

    public int getMaxWorkers() { return level * 5; }

    /** Production periodique : chaque hut produit une ressource liee a son metier. */
    public void produceTick(net.minecraft.server.level.ServerLevel serverLevel) {
        for (ColonyJob job : huts.values()) {
            ItemStack icon = job.getIcon();
            addToStock(icon);
        }
        //XP de colonie fictive : level up tous les 100 objets stockes
        int total = warehouse.values().stream().mapToInt(ColonyResource::amount).sum();
        int expectedLevel = 1 + total / 100;
        if (expectedLevel > this.level) setLevel(expectedLevel);
    }

    /**
     * Fin d'une journee de colonie : nourriture + salaires.
     * Chaque ouvrier mange 1 pain et recoit son salaire en emeraudes depuis l'entrepot.
     * Sans nourriture suffisante, les colons refusent de travailler (bonus de production coupe).
     */
    public boolean feedAndPayWorkers() {
        int neededFood = Math.max(1, getWorkerCount());
        int eaten = removeFromStock("minecraft:bread", neededFood);
        boolean fed = eaten >= neededFood;
        if (fed) {
            int totalWage = 0;
            for (ColonyJob job : huts.values()) totalWage += job.getWage();
            // On paie dans la limite du stock disponible : les non-payes reduisent la motivation.
            int paid = removeFromStock("minecraft:emerald", totalWage);
            lastWagePaid = totalWage > 0 ? paid * 100 / totalWage : 100;
        } else {
            lastWagePaid = 0;
        }
        return fed;
    }

    private int lastWagePaid = 100;
    public int getWageCoverage() { return lastWagePaid; }

    /** Retourne false si la colonie ne peut pas nourrir ses ouvriers ce jour-la. */
    public void advanceDay() {
        feedAndPayWorkers();
    }

    private void setLevel(int newLevel) {
        this.level = Math.min(newLevel, 5);
    }

    /** Tick appele regulierement par ColonyManager ; gere le cycle jour/nuit de colonie. */
    public void colonyTick(long gameTime) {
        dayTimer++;
        if (dayTimer >= COLONY_DAY_TICKS / 4) { // un "jour" toutes les ~5 min pour le gameplay
            dayTimer = 0;
            advanceDay();
        }
    }

    // ---------- NBT ----------
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", id);
        tag.putString("Name", name);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putLong("Center", centerPos.asLong());
        tag.putInt("Radius", radius);
        tag.putInt("Level", level);
        tag.putLong("DayTimer", dayTimer);

        ListTag hutList = new ListTag();
        huts.forEach((pos, job) -> {
            CompoundTag t = new CompoundTag();
            t.putLong("Pos", pos.asLong());
            t.putString("Job", job.getId());
            hutList.add(t);
        });
        tag.put("Huts", hutList);

        ListTag workerList = new ListTag();
        workers.forEach(uuid -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("Uuid", uuid);
            workerList.add(t);
        });
        tag.put("Workers", workerList);

        ListTag whList = new ListTag();
        warehouse.values().forEach(r -> whList.add(r.toTag()));
        tag.put("WarehouseLots", whList);
        return tag;
    }

    public static Colony load(CompoundTag tag) {
        Colony colony = new Colony(tag.getInt("Id"));
        colony.name = tag.getString("Name");
        if (tag.hasUUID("Owner")) colony.ownerUUID = tag.getUUID("Owner");
        colony.centerPos = BlockPos.of(tag.getLong("Center"));
        colony.radius = tag.getInt("Radius");
        colony.level = Math.max(1, tag.getInt("Level"));
        colony.dayTimer = tag.getLong("DayTimer");
        ListTag hutList = tag.getList("Huts", Tag.TAG_COMPOUND);
        for (int i = 0; i < hutList.size(); i++) {
            CompoundTag t = hutList.getCompound(i);
            ColonyJob job = ColonyJob.fromId(t.getString("Job"));
            if (job != null) colony.huts.put(BlockPos.of(t.getLong("Pos")), job);
        }
        ListTag workerList = tag.getList("Workers", Tag.TAG_COMPOUND);
        for (int i = 0; i < workerList.size(); i++) {
            colony.workers.add(workerList.getCompound(i).getUUID("Uuid"));
        }
        // Nouveau format : liste de lots
        if (tag.contains("WarehouseLots", Tag.TAG_LIST)) {
            ListTag whList = tag.getList("WarehouseLots", Tag.TAG_COMPOUND);
            for (int i = 0; i < whList.size(); i++) {
                ColonyResource res = ColonyResource.fromTag(whList.getCompound(i));
                if (res != null) colony.warehouse.put(res.key(), res);
            }
        }
        // Retro-compat ancien format (map cle->int) fusionnee si presente
        if (tag.contains("Warehouse", Tag.TAG_COMPOUND)) {
            CompoundTag wh = tag.getCompound("Warehouse");
            for (String key : wh.getAllKeys()) colony.addToStock(key, wh.getInt(key));
        }
        return colony;
    }

    /** Version reduite envoyee au client pour l'affichage de l'UI. */
    public CompoundTag toSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", id);
        tag.putString("Name", name);
        tag.putInt("Level", level);
        tag.putInt("Workers", workers.size());
        tag.putInt("MaxWorkers", getMaxWorkers());
        tag.putInt("Huts", huts.size());
        tag.putInt("WageCoverage", lastWagePaid);
        ListTag whList = new ListTag();
        warehouse.values().forEach(r -> whList.add(r.toTag()));
        tag.put("WarehouseLots", whList);
        return tag;
    }
}
