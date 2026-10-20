package com.colonies.colony;

import com.colonies.api.ColonyJob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

/** Le colon : PNJ qui se rend a sa hut et travaille pour sa colonie. */
public class Colonist extends Villager {
    private static final EntityDataAccessor<String> DATA_JOB =
            SynchedEntityData.defineId(Colonist.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_COLONY_NAME =
            SynchedEntityData.defineId(Colonist.class, EntityDataSerializers.STRING);

    private int colonyId = -1;
    private BlockPos workPosition = BlockPos.ZERO;

    public Colonist(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_JOB, "");
        builder.define(DATA_COLONY_NAME, "");
    }

    public String getJobName() { return this.entityData.get(DATA_JOB); }
    public void setJobName(String job) { this.entityData.set(DATA_JOB, job); }

    public String getColonyName() { return this.entityData.get(DATA_COLONY_NAME); }
    public void setColonyName(String name) { this.entityData.set(DATA_COLONY_NAME, name); }

    public int getColonyId() { return colonyId; }
    public void setColonyId(int id) { this.colonyId = id; }

    public BlockPos getWorkPosition() { return workPosition; }
    public void setWorkPosition(BlockPos pos) { this.workPosition = pos.immutable(); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !isAlive() || colonyId == -1) return;
        // Tous les 2 secondes environ : aller travailler a la hut
        if (level().getGameTime() % 40 == 0) {
            boolean farFromWork = position().distanceToSqr(workPosition.getCenter()) > 6.25;
            if (farFromWork) {
                getNavigation().moveTo(workPosition.getX() + 0.5, workPosition.getY(),
                        workPosition.getZ() + 0.5, 1.0);
            } else {
                getNavigation().stop();
                // Regarde la hut : calcul manuel des angles (pas de lookAt(Vec3) sur Mob en 1.21.1)
                net.minecraft.world.phys.Vec3 delta = workPosition.getCenter().subtract(position());
                double horizDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                float yaw = (float) (Math.atan2(-delta.x, delta.z) * (180.0 / Math.PI));
                float pitch = (float) (-Math.atan2(delta.y, horizDist) * (180.0 / Math.PI));
                setRot(yaw, pitch);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ColoniesColonyId", this.colonyId);
        tag.putString("ColoniesJob", this.getJobName());
        tag.putString("ColoniesColonyName", this.getColonyName());
        tag.putLong("ColoniesWorkPos", this.workPosition.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.colonyId = tag.getInt("ColoniesColonyId");
        this.setJobName(tag.getString("ColoniesJob"));
        this.setColonyName(tag.getString("ColoniesColonyName"));
        this.workPosition = BlockPos.of(tag.getLong("ColoniesWorkPos"));
    }
}
