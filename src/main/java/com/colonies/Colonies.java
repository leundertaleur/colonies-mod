package com.colonies;

import com.colonies.commands.ColonyCommands;
import com.colonies.network.SyncColonyPayload;
import com.colonies.registration.ColonyBlockEntities;
import com.colonies.registration.ColonyBlocks;
import com.colonies.registration.ColonyCreativeTab;
import com.colonies.registration.ColonyDataComponents;
import com.colonies.registration.ColonyEntities;
import com.colonies.registration.ColonyItems;
import com.colonies.registration.ColonyMenus;
import com.colonies.worldgen.ColonyStructures;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.colonies.colony.ColonyManager;

/**
 * Colonies - un mod de colonie inspire de MineColonies.
 * Le joueur place un bloc de centre de colonie pour fonder une colonie,
 * construit des cabanes d'ouvriers qui recrutent des PNJ colons, et gere ses ressources.
 */
@Mod(Colonies.MODID)
public class Colonies {
    public static final String MODID = "colonies";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public Colonies(IEventBus modEventBus, ModContainer modContainer) {
        // Registries
        ColonyBlocks.BLOCKS.register(modEventBus);
        ColonyItems.ITEMS.register(modEventBus);
        ColonyBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ColonyEntities.ENTITY_TYPES.register(modEventBus);
        ColonyMenus.MENUS.register(modEventBus);
        ColonyDataComponents.DATA_COMPONENTS.register(modEventBus);
        ColonyCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ColonyStructures.register(modEventBus);

        // Events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[Colonies] Initialisation du mod de colonie !");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncColonyPayload.TYPE, SyncColonyPayload.STREAM_CODEC,
                SyncColonyPayload::handle);
        registrar.playToClient(com.colonies.network.OpenColonyMenuPayload.TYPE,
                com.colonies.network.OpenColonyMenuPayload.STREAM_CODEC,
                com.colonies.network.OpenColonyMenuPayload::handle);
        registrar.playToServer(com.colonies.network.RequestOpenColonyScreenPayload.TYPE,
                com.colonies.network.RequestOpenColonyScreenPayload.STREAM_CODEC,
                com.colonies.network.RequestOpenColonyScreenPayload::handle);
        registrar.playToClient(com.colonies.network.ShowColonyScreenPayload.TYPE,
                com.colonies.network.ShowColonyScreenPayload.STREAM_CODEC,
                com.colonies.network.ShowColonyScreenPayload::handle);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ColonyCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Coeur du mod : fait avancer les colonies (production, niveau, synchro UI)
        ColonyManager.getInstance().tick(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            ColonyManager manager = ColonyManager.getInstance();
            manager.load(player.level().getServer());
            if (manager.hasColonyFor(player)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.colonies.welcome_back"),
                        false);
            }
        }
    }

    // L'enregistrement de l'ecran client se fait dans com.colonies.client.ClientSetup
}
