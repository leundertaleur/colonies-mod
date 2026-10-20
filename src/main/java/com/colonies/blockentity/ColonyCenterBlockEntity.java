package com.colonies.blockentity;

import com.colonies.Colonies;
import com.colonies.colony.Colony;
import com.colonies.colony.ColonyManager;
import com.colonies.registration.ColonyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** BlockEntity du centre de colonie : attache la colonie au bloc et fournit l'UI. */
public class ColonyCenterBlockEntity extends BlockEntity implements MenuProvider {
    private int colonyId = -1;

    public ColonyCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ColonyBlockEntities.COLONY_CENTER_BE.get(), pos, state);
    }

    /** Fonde une colonie si celle-ci n'existe pas encore, puis retourne son id. */
    public int ensureColony(Player player) {
        if (colonyId == -1 && level != null && !level.isClientSide) {
            ColonyManager manager = ColonyManager.getInstance();
            Colony colony = manager.createColony(player, worldPosition,
                    Component.translatable("text.colonies.default_name").getString());
            this.colonyId = colony.getId();
            setChanged();
            manager.save(level.getServer());
        }
        return colonyId;
    }

    public int getColonyId() { return colonyId; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.colonies.colony_screen");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.colonies.client.ColonyMenu(containerId, inventory, colonyId);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ColonyId", colonyId);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.colonyId = input.getIntOr("ColonyId", -1);
    }
}
