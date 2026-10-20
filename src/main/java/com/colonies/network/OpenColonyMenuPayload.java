package com.colonies.network;

import com.colonies.Colonies;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Demande l'ouverture du GUI de colonie cote client (serveur -> client).
 * En NeoForge 21.1.x, openMenu() cote serveur n'affiche PAS automatiquement
 * un ecran custom sans NetworkHook : ce payload declenche donc la reponse
 * client RequestOpenColonyScreenPayload qui rouvre le menu via le flux officiel
 * ServerPlayer.openMenu(MenuProvider, writer), puis ColonyCenterBlockEntity
 * envoie ShowColonyScreenPayload pour afficher l'ecran avec le menu synchronise.
 */
public record OpenColonyMenuPayload(int colonyId) implements CustomPacketPayload {
    public static final Type<OpenColonyMenuPayload> TYPE = new Type<>(Colonies.id("open_colony_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenColonyMenuPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(OpenColonyMenuPayload::new, OpenColonyMenuPayload::colonyId);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenColonyMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Cote client : on redemande au serveur d'ouvrir le container proprement.
            context.reply(new RequestOpenColonyScreenPayload(payload.colonyId()));
        });
    }
}
