package com.colonies.colony;

import com.colonies.api.ColonyJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Represente une colonie : territoire, nom, cabanes, ouvriers et stock. */
public class Colony {
    private final int id;
    private String name = "Colonie";
    private UUID ownerUUID;
    private BlockPos centerPos = BlockPos.ZERO;
    private int radius = 20;
    private int level = 1;

    /** Positions des cabanes d'ouvriers -> metier associe. */
    private final Map<BlockPos, ColonyJob> huts = new HashMap<>();
    /** Colons actifs (uuid). */
    private final List<UUID> workers = new ArrayList<>();
    /** Stock de la colonie (item -> quantite simplifiee). */
    private final Map<String, Integer> warehouse = new HashMap<>();

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

    public void addToStock(ItemStack stack) {
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        warehouse.merge(key, stack.getCount(), Integer::sum);
    }

    public Map<String, Integer> getWarehouse() { return warehouse; }

    public int getMaxWorkers() { return level * 5; }

    /** Production periodique : chaque hut produit une ressource liee a son metier. */
    public void produceTick(net.minecraft.server.level.ServerLevel serverLevel) {
        for (ColonyJob job : huts.values()) {
            ItemStack icon = job.getIcon();
            addToStock(icon);
        }
        //XP de colonie fictive : level up tous les 100 objets stockes
        int total = warehouse.values().stream().mapToInt(Integer::intValue).sum();
        int expectedLevel = 1 + total / 100;
        if (expectedLevel > this.level) setLevel(expectedLevel);
    }

    private void setLevel(int newLevel) {
        this.level = Math.min(newLevel, 5);
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

        CompoundTag wh = new CompoundTag();
        warehouse.forEach(wh::putInt);
        tag.put("Warehouse", wh);
        return tag;
    }

    public static Colony load(CompoundTag tag) {
        Colony colony = new Colony(tag.getInt("Id"));
        colony.name = tag.getString("Name");
        if (tag.hasUUID("Owner")) colony.ownerUUID = tag.getUUID("Owner");
        colony.centerPos = BlockPos.of(tag.getLong("Center"));
        colony.radius = tag.getInt("Radius");
        colony.level = Math.max(1, tag.getInt("Level"));
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
        CompoundTag wh = tag.getCompound("Warehouse");
        for (String key : wh.getAllKeys()) colony.warehouse.put(key, wh.getInt(key));
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
        CompoundTag wh = new CompoundTag();
        warehouse.forEach(wh::putInt);
        tag.put("Warehouse", wh);
        return tag;
    }
}
