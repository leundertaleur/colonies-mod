package com.colonies.blockentity;

import com.colonies.Colonies;
import com.colonies.api.ColonyJob;
import com.colonies.block.WorkerHutBlock;
import com.colonies.colony.Colonist;
import com.colonies.colony.Colony;
import com.colonies.colony.ColonyManager;
import com.colonies.registration.ColonyBlockEntities;
import com.colonies.registration.ColonyEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** BlockEntity des cabanes : enregistre la hut aupres de la colonie et recrute un colon. */
public class WorkerHutBlockEntity extends BlockEntity {
    private int colonyId = -1;
    private boolean initialized = false;
    @Nullable
    private UUID workerUuid = null;

    public WorkerHutBlockEntity(BlockPos pos, BlockState state) {
        super(ColonyBlockEntities.WORKER_HUT_BE.get(), pos, state);
    }

    /** Appelle periodiquement par le ticker du bloc (WorkerHutBlock#getTicker). */
    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T be) {
        if (be instanceof WorkerHutBlockEntity hut && level instanceof ServerLevel serverLevel) {
            hut.tick(serverLevel);
        }
    }

    private void tick(ServerLevel level) {
        if (!initialized) {
            initialize(level);
            initialized = true;
        }
        // Recrutement : toutes les ~10s si la hut n'a pas d'ouvrier
        if (level.getGameTime() % 200 == 0) {
            recruitIfNeeded(level);
        }
    }

    private void initialize(ServerLevel level) {
        ColonyManager manager = ColonyManager.getInstance();
        manager.load(level.getServer());
        for (Colony colony : manager.getColonies()) {
            if (colony.isInside(worldPosition)) {
                this.colonyId = colony.getId();
                ColonyJob job = getJob();
                if (job != null) colony.addHut(worldPosition, job);
                setChanged();
                break;
            }
        }
    }

    @Nullable
    public ColonyJob getJob() {
        return getBlockState().getBlock() instanceof WorkerHutBlock block ? block.getJob() : null;
    }

    public void onRemove(Level level) {
        if (colonyId != -1) {
            ColonyManager.getInstance().getById(colonyId).ifPresent(c -> {
                c.removeHut(worldPosition);
                if (workerUuid != null) c.removeWorker(workerUuid);
            });
        }
        net.minecraft.world.entity.npc.Villager worker = findWorker(level);
        if (worker != null) worker.discard();
    }

    private void recruitIfNeeded(ServerLevel level) {
        ColonyJob existingJob = getJob();
        if (colonyId == -1 || existingJob == null) return;
        if (findWorker(level) != null) return; // deja peuple
        ColonyManager manager = ColonyManager.getInstance();
        var opt = manager.getById(colonyId);
        if (opt.isEmpty()) return;
        Colony colony = opt.get();
        if (colony.getWorkerCount() >= colony.getMaxWorkers()) return;

        net.minecraft.world.entity.npc.Villager worker;
        ColonyJob job = getJob();
        if (job.isKnight()) {
            com.colonies.colony.Knight knight = ColonyEntities.KNIGHT.get().create(level);
            if (knight == null) return;
            knight.setColonyId(colony.getId());
            knight.setColonyName(colony.getName());
            knight.setPatrolCenter(worldPosition);
            worker = knight;
        } else {
            Colonist colonist = ColonyEntities.COLONIST.get().create(level);
            if (colonist == null) return;
            colonist.setColonyId(colony.getId());
            colonist.setColonyName(colony.getName());
            colonist.setJobName(job.getId());
            colonist.setWorkPosition(worldPosition);
            worker = colonist;
        }
        BlockPos spawn = worldPosition.above();
        worker.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0f, 0f);
        worker.setPersistenceRequired();
        level.addFreshEntity(worker);
        this.workerUuid = worker.getUUID();
        colony.addWorker(worker.getUUID());
        manager.save(level.getServer());
        Colonies.LOGGER.info("Ouvrier '{}' recrute pour la colonie {}", job.getId(), colony.getName());
    }

    @Nullable
    private net.minecraft.world.entity.npc.Villager findWorker(Level level) {
        if (workerUuid == null) return null;
        for (net.minecraft.world.entity.npc.Villager entity :
                level.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class,
                        new net.minecraft.world.phys.AABB(worldPosition).inflate(16))) {
            if ((entity instanceof Colonist || entity instanceof com.colonies.colony.Knight)
                    && entity.getUUID().equals(workerUuid) && entity.isAlive()) {
                return entity;
            }
        }
        return null;
    }

    public int getColonyId() { return colonyId; }

    // Signatures officielles verifiees sur NeoForge 21.1.262 / MC 1.21.1 (javap sur BlockEntity.class) :
    //   protected void saveAdditional(CompoundTag, HolderLookup.Provider)
    //   protected void loadAdditional(CompoundTag, HolderLookup.Provider)
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ColonyId", colonyId);
        tag.putBoolean("Initialized", initialized);
        if (workerUuid != null) tag.putUUID("WorkerUuid", workerUuid);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.colonyId = tag.getInt("ColonyId");
        this.initialized = tag.getBoolean("Initialized");
        if (tag.hasUUID("WorkerUuid")) this.workerUuid = tag.getUUID("WorkerUuid");
    }
}
