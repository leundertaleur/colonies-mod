package com.colonies.network;

import com.colonies.Colonies;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serveur -> client (envoye APRES l'appel officiel a openMenu) :
 * le container ColonyMenu est deja cree et synchronise cote client ;
 * ce payload se contente d'afficher ColonyScreen avec ce menu courant.
 */
public record ShowColonyScreenPayload(int colonyId, String colonyName) implements CustomPacketPayload {
    public static final Type<ShowColonyScreenPayload> TYPE = new Type<>(Colonies.id("show_colony_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowColonyScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShowColonyScreenPayload::colonyId,
                    ByteBufCodecs.STRING_UTF8, ShowColonyScreenPayload::colonyName,
                    ShowColonyScreenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShowColonyScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            if (mc.player.containerMenu instanceof com.colonies.menu.ColonyMenu menu
                    && menu.getColonyId() == payload.colonyId()) {
                Component title = Component.literal(
                        payload.colonyName().isEmpty() ? "Colonie" : payload.colonyName());
                mc.setScreen(new com.colonies.client.ColonyScreen(menu, mc.player.getInventory(), title));
            }
        });
    }
}
