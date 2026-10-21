package com.colonies.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * Le chevalier de colonie : patrouille autour du poste assigne et attaque les monstres
 * hostiles qui entrent dans le territoire. Inspire du Guard de MineColonies (version simple).
 */
public class Knight extends Villager {
    private static final EntityDataAccessor<String> DATA_COLONY_NAME =
            SynchedEntityData.defineId(Knight.class, EntityDataSerializers.STRING);

    private int colonyId = -1;
    private BlockPos patrolCenter = BlockPos.ZERO;
    private int patrolRadius = 8;

    public Knight(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes()
                .set(Attributes.MAX_HEALTH, 40.0)
                .set(Attributes.ATTACK_DAMAGE, 6.0)
                .set(Attributes.MOVEMENT_SPEED, 0.28)
                .set(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLONY_NAME, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new KnightAttackGoal(this));
        this.goalSelector.addGoal(4, new PatrolAroundPostGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        // Cibles : tous les monstres hostiles (et pas les colons/joueurs)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,
                new NearestAttackableTargetGoal<>(this, Monster.class, true,
                        target -> Math.abs(target.getX() - patrolCenter.getX()) <= patrolRadius + 8
                                && Math.abs(target.getZ() - patrolCenter.getZ()) <= patrolRadius + 8));
    }

    public String getColonyName() { return this.entityData.get(DATA_COLONY_NAME); }
    public void setColonyName(String name) { this.entityData.set(DATA_COLONY_NAME, name); }

    public int getColonyId() { return colonyId; }
    public void setColonyId(int id) { this.colonyId = id; }

    public BlockPos getPatrolCenter() { return patrolCenter; }
    public void setPatrolCenter(BlockPos pos) { this.patrolCenter = pos.immutable(); }

    /** L'IA n'attaque que dans un rayon donne autour du poste. */
    public boolean canChase(LivingEntity target) {
        return target.distanceToSqr(patrolCenter.getCenter())
                <= (double) (patrolRadius + 6) * (patrolRadius + 6);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isAlive() && !level().getEntities(this,
                getBoundingBox().inflate(1.5)).isEmpty() && random.nextFloat() < 0.002f) {
            setNoActionTime(100); // evite de despawner/fuguer quand un joueur est proche
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ColoniesColonyId", this.colonyId);
        tag.putString("ColoniesColonyName", this.getColonyName());
        tag.putLong("ColoniesPatrolCenter", this.patrolCenter.asLong());
        tag.putInt("ColoniesPatrolRadius", this.patrolRadius);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.colonyId = tag.getInt("ColoniesColonyId");
        this.setColonyName(tag.getString("ColoniesColonyName"));
        this.patrolCenter = BlockPos.of(tag.getLong("ColoniesPatrolCenter"));
        if (tag.contains("ColoniesPatrolRadius")) this.patrolRadius = tag.getInt("ColoniesPatrolRadius");
    }

    /** Attaque au contact, seulement si la cible est dans la zone de patrouille. */
    private static class KnightAttackGoal extends MeleeAttackGoal {
        private final Knight knight;

        public KnightAttackGoal(Knight knight) {
            super(knight, 1.0, true);
            this.knight = knight;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && knight.canChase(knight.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && knight.canChase(knight.getTarget());
        }
    }

    /** Petite ronde aleatoire autour du poste de garde. */
    private static class PatrolAroundPostGoal extends Goal {
        private final Knight knight;
        private double targetX, targetY, targetZ;
        private int cooldown;

        public PatrolAroundPostGoal(Knight knight) {
            this.knight = knight;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--cooldown > 0) return false;
            BlockPos center = knight.patrolCenter;
            int r = knight.patrolRadius;
            var rnd = knight.getRandom();
            this.targetX = center.getX() + 0.5 + (rnd.nextDouble() * 2 - 1) * r;
            this.targetY = center.getY();
            this.targetZ = center.getZ() + 0.5 + (rnd.nextDouble() * 2 - 1) * r;
            return knight.getTarget() == null;
        }

        @Override
        public void start() {
            this.cooldown = 60 + knight.getRandom().nextInt(120);
            knight.getNavigation().moveTo(targetX, targetY, targetZ, 0.7);
        }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            if (knight.getNavigation().isDone()) this.cooldown = 40;
        }
    }
}
