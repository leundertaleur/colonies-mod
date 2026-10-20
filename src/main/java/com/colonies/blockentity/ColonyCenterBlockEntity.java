package com.colonies.blockentity;

import com.colonies.colony.ColonyManager;
import com.colonies.registration.ColonyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

/** BlockEntity du centre de colonie : attache la colonie au bloc et ouvre l'UI. */
public class ColonyCenterBlockEntity extends BlockEntity {
    private int colonyId = -1;

    public ColonyCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ColonyBlockEntities.COLONY_CENTER_BE.get(), pos, state);
    }

    /** Fonde une colonie si celle-ci n'existe pas encore, puis retourne son id. */
    public int ensureColony(Player player) {
        if (colonyId == -1 && level != null && !level.isClientSide) {
            ColonyManager manager = ColonyManager.getInstance();
            com.colonies.colony.Colony colony = manager.createColony(player, worldPosition,
                    Component.translatable("text.colonies.default_name").getString());
            this.colonyId = colony.getId();
            setChanged();
            manager.save(level.getServer());
        }
        return colonyId;
    }

    public int getColonyId() { return colonyId; }

    /** Clic droit sur le centre : fonde la colonie puis demande l'ouverture du GUI client. */
    public void openGui(ServerPlayer player) {
        if (ensureColony(player) != -1) {
            PacketDistributor.sendToPlayer(player, new com.colonies.network.OpenColonyMenuPayload(colonyId));
        }
    }

    // Signatures officielles verifiees sur NeoForge 21.1.262 / MC 1.21.1 (javap sur BlockEntity.class) :
    //   protected void saveAdditional(CompoundTag, HolderLookup.Provider)
    //   protected void loadAdditional(CompoundTag, HolderLookup.Provider)
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ColonyId", colonyId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.colonyId = tag.getInt("ColonyId");
    }
}
