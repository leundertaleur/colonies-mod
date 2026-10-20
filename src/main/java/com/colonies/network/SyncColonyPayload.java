package com.colonies.network;

import com.colonies.Colonies;
import com.colonies.colony.ColonyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Envoie les donnees d'une colonie du serveur vers le client (pour l'UI). */
public record SyncColonyPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncColonyPayload> TYPE = new Type<>(Colonies.id("sync_colony"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncColonyPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> buf.writeNbt(payload.data()),
                    buf -> new SyncColonyPayload(buf.readNbt()));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncColonyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ColonyManager.getInstance().handleClientSync(payload.data()));
    }
}
