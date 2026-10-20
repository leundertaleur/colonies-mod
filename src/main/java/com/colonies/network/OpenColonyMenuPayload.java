package com.colonies.network;

import com.colonies.Colonies;
import com.colonies.menu.ColonyMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Demande l'ouverture du menu de colonie cote client.
 * Le serveur envoie (colonyId, nom) ; le client construit ColonyMenu localement
 * puis l'installe comme "menu actif" sans networkChannel (mode lecture seule).
 * C'est la methode standard pour ouvrir un custom menu sans NetworkHook dans NeoForge 21.1.
 */
public record OpenColonyMenuPayload(int colonyId, String colonyName) implements CustomPacketPayload {
    public static final Type<OpenColonyMenuPayload> TYPE = new Type<>(Colonies.id("open_colony_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenColonyMenuPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenColonyMenuPayload::colonyId,
                    ByteBufCodecs.STRING_UTF8, OpenColonyMenuPayload::colonyName,
                    OpenColonyMenuPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenColonyMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            ColonyMenu menu = new ColonyMenu(-128, mc.player.getInventory(), payload.colonyId());
            // containerId negatif => aucun slot synchronise avec le serveur : menu local lecture seule.
            mc.player.containerMenu = menu;
            Component title = Component.literal(payload.colonyName().isEmpty() ? "Colonie" : payload.colonyName());
            mc.setScreen(new com.colonies.client.ColonyScreen(menu, mc.player.getInventory(), title));
            // Force une synchro immediate des donnees de colonie vers ce client :
            // sans hook reseau, c'est la facon portable d'ouvrir un custom GUI en 21.1.x.
            try {
                net.minecraft.server.MinecraftServer server = mc.level.getServer();
                if (server != null && mc.player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    com.colonies.colony.ColonyManager.getInstance()
                            .sendSyncTo(sp, payload.colonyId());
                }
            } catch (Exception ignored) {}
        });
    }
}
