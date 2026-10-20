package com.colonies.commands;

import com.colonies.colony.Colony;
import com.colonies.colony.ColonyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Commandes /colonies pour gerer ses colonies. */
public class ColonyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("colonies")
                .then(Commands.literal("list").executes(ColonyCommands::list))
                .then(Commands.literal("info").executes(ColonyCommands::info))
                .then(Commands.literal("name")
                        .then(Commands.argument("nouveau_nom", StringArgumentType.string())
                                .executes(ColonyCommands::rename)))
                .then(Commands.literal("delete").executes(ColonyCommands::delete)));
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        var colonies = ColonyManager.getInstance().getColonies();
        if (colonies.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.colonies.none"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("command.colonies.list_header"), false);
        for (Colony c : colonies) {
            ctx.getSource().sendSuccess(() -> Component.literal(" - [" + c.getId() + "] " + c.getName()
                    + " (niveau " + c.getLevel() + ", " + c.getWorkerCount() + " ouvriers)")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        return colonies.size();
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        var player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        var opt = ColonyManager.getInstance().getColonyFor(player);
        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.colonies.no_colony"));
            return 0;
        }
        Colony c = opt.get();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Colonie '" + c.getName() + "' | Niveau " + c.getLevel()
                        + " | Ouvriers " + c.getWorkerCount() + "/" + c.getMaxWorkers()
                        + " | Cabanes " + c.getHuts().size()
                        + " | Centre " + c.getCenterPos().toShortString()), true);
        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> ctx) {
        var player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        String name = StringArgumentType.getString(ctx, "nouveau_nom");
        var opt = ColonyManager.getInstance().getColonyFor(player);
        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.colonies.no_colony"));
            return 0;
        }
        opt.get().setName(name);
        ColonyManager.getInstance().save(ctx.getSource().getServer());
        final String finalName = name;
        ctx.getSource().sendSuccess(() -> Component.translatable("command.colonies.renamed", finalName), true);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx) {
        var player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        var opt = ColonyManager.getInstance().getColonyFor(player);
        if (opt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.colonies.no_colony"));
            return 0;
        }
        ColonyManager.getInstance().removeColony(opt.get().getId());
        ColonyManager.getInstance().save(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable("command.colonies.deleted"), true);
        return 1;
    }
}
