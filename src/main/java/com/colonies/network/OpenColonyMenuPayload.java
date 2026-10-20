package com.colonies.network;

import com.colonies.Colonies;
import com.colonies.menu.ColonyMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
            ClientLevel level = mc.level;
            if (level == null || mc.player == null) return;
            ColonyMenu menu = new ColonyMenu(
                    level.addContainerMenu(-128), mc.player.getInventory(), payload.colonyId());
            // containerId negatif => aucune synchronisation d'slots avec le serveur : menu local.
            mc.player.containerMenu = menu;
            mc.setScreen(new com.colonies.client.ColonyScreen(menu, mc.player.getInventory(),
                    Component.literal(payload.colonyName())));
        });
    }
}
