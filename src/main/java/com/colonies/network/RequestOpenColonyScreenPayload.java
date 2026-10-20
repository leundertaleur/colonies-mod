package com.colonies.network;

import com.colonies.Colonies;
import com.colonies.colony.ColonyManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> serveur : "ouvre l'ecran de la colonie colonyId".
 * Le serveur construit un MenuProvider leger et appelle openMenu officiel,
 * ce qui synchronise le container correctement (pas d'acces aux API privees).
 */
public record RequestOpenColonyScreenPayload(int colonyId) implements CustomPacketPayload {
    public static final Type<RequestOpenColonyScreenPayload> TYPE =
            new Type<>(Colonies.id("request_open_colony_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenColonyScreenPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeVarInt(payload.colonyId()),
                    buf -> new RequestOpenColonyScreenPayload(buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestOpenColonyScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                ColonyManager manager = ColonyManager.getInstance();
                manager.getById(payload.colonyId()).ifPresent(colony -> {
                    String name = colony.getName();
                    MenuProvider provider = new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal(name.isEmpty() ? "Colonie" : name);
                        }
                        @Override
                        public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
                            return new com.colonies.menu.ColonyMenu(windowId, inv, payload.colonyId());
                        }
                    };
                    // Pousse les donnees de colonie AVANT l'ouverture pour un affichage immediat.
                    manager.sendSyncTo(sp, payload.colonyId());
                    sp.openMenu(provider, buf -> buf.writeInt(payload.colonyId()));
                    // openMenu cote serveur n'affiche pas l'ecran custom sans NetworkHook :
                    // on envoie explicitement le screen une fois le container synchronise.
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                            new ShowColonyScreenPayload(payload.colonyId(), name));
                });
            }
        });
    }
}
