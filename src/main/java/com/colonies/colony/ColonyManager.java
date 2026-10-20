package com.colonies.colony;

import com.colonies.Colonies;
import com.colonies.client.ColonyDataSync;
import com.colonies.network.SyncColonyPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Singleton qui gere toutes les colonies (serveur) + cache client pour l'UI. */
public class ColonyManager {
    private static final ColonyManager INSTANCE = new ColonyManager();
    public static ColonyManager getInstance() { return INSTANCE; }

    private final Map<Integer, Colony> colonies = new HashMap<>();
    private int nextId = 0;
    private boolean loaded = false;

    public List<Colony> getColonies() { return new ArrayList<>(colonies.values()); }

    public Optional<Colony> getById(int id) { return Optional.ofNullable(colonies.get(id)); }

    public Optional<Colony> getColonyAt(BlockPos pos) {
        for (Colony c : colonies.values()) if (c.isInside(pos)) return Optional.of(c);
        return Optional.empty();
    }

    public Optional<Colony> getColonyFor(Player player) {
        for (Colony c : colonies.values())
            if (player.getUUID().equals(c.getOwner())) return Optional.of(c);
        return Optional.empty();
    }

    public boolean hasColonyFor(Player player) { return getColonyFor(player).isPresent(); }

    /** Cree une nouvelle colonie et retourne son identifiant. */
    public Colony createColony(Player owner, BlockPos center, String name) {
        Colony colony = new Colony(nextId++);
        colony.setOwner(owner.getUUID());
        colony.setCenterPos(center);
        colony.setName(name);
        colonies.put(colony.getId(), colony);
        Colonies.LOGGER.info("Colonie '{}' (id {}) fondee par {}", name, colony.getId(),
                owner.getName().getString());
        return colony;
    }

    public void removeColony(int id) { colonies.remove(id); }

    /** Tick global (serveur) : production + niveau + synchro vers le joueur concerne. */
    public void tick(MinecraftServer server) {
        if (!loaded) load(server);
        long time = server.overworld().getGameTime();
        if (time % 200 == 0 && !colonies.isEmpty()) { // toutes les 10 secondes
            for (Colony colony : colonies.values()) {
                colony.produceTick(server.overworld());
            }
            save(server);
        }
        if (time % 40 == 0) {
            for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                getColonyFor(sp).ifPresent(colony ->
                        PacketDistributor.sendToPlayer(sp, new SyncColonyPayload(colony.toSyncTag())));
            }
        }
    }

    // ---------- Cache client ----------
    /** Recoit la synchro du serveur et stocke une copie locale pour l'UI (client uniquement). */
    public void handleClientSync(CompoundTag tag) {
        ColonyDataSync.put(tag.getInt("Id"), tag);
    }

    /** Accès client sur (id) -> CompoundTag synchronisé. */
    public static CompoundTag getSyncedColony(int id) {
        return ColonyDataSync.get(id);
    }

    // ---------- Persistance (fichier colonies.dat dans le monde) ----------
    public void load(MinecraftServer server) {
        if (loaded || server == null) return;
        loaded = true;
        CompoundTag data = readFromDisk(server);
        if (data != null && data.contains("Colonies", Tag.TAG_LIST)) {
            colonies.clear();
            ListTag list = data.getList("Colonies", Tag.TAG_COMPOUND);
            int maxId = -1;
            for (int i = 0; i < list.size(); i++) {
                Colony colony = Colony.load(list.getCompound(i));
                colonies.put(colony.getId(), colony);
                maxId = Math.max(maxId, colony.getId());
            }
            nextId = maxId + 1;
        }
    }

    public void save(MinecraftServer server) {
        if (server == null) return;
        CompoundTag data = new CompoundTag();
        ListTag list = new ListTag();
        for (Colony colony : colonies.values()) list.add(colony.save());
        data.put("Colonies", list);
        writeToDisk(server, data);
    }

    private CompoundTag readFromDisk(MinecraftServer server) {
        File file = getSaveFile(server);
        if (file.exists()) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
                return net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            } catch (Exception e) {
                Colonies.LOGGER.error("Erreur de lecture des colonies", e);
            }
        }
        return null;
    }

    private void writeToDisk(MinecraftServer server, CompoundTag data) {
        try {
            File file = getSaveFile(server);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            Path path = file.toPath();
            net.minecraft.nbt.NbtIo.writeCompressed(data, path);
        } catch (Exception e) {
            Colonies.LOGGER.error("Erreur de sauvegarde des colonies", e);
        }
    }

    private File getSaveFile(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("colonies.dat").toFile();
    }
}
