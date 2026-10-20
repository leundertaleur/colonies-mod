package com.colonies.block;

import com.colonies.api.ColonyJob;
import com.colonies.blockentity.WorkerHutBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Cabane d'ouvrier : abrite un colon avec un metier. */
public class WorkerHutBlock extends BaseEntityBlock {
    public static final MapCodec<WorkerHutBlock> CODEC = simpleCodec(WorkerHutBlock::new);
    public static final VoxelShape SHAPE = box(1, 0, 1, 15, 8, 15);

    private final ColonyJob job;

    /** Constructeur sans metier (utilisé par le codec / les états de bloc sauvegardés). */
    public WorkerHutBlock(Properties properties) {
        this(null, properties);
    }

    public WorkerHutBlock(ColonyJob job, Properties properties) {
        super(properties);
        this.job = job;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public @Nullable ColonyJob getJob() { return job; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkerHutBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        return level.isClientSide ? null : WorkerHutBlockEntity::serverTick;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WorkerHutBlockEntity hut) {
            hut.onRemove(level);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
