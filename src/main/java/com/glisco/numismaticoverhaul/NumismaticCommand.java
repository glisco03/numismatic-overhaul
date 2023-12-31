package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.offline.OfflineDataLookup;
import io.wispforest.owo.ops.TextOps;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NumismaticCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("numismatic")
                .then(literal("balance").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(argument("player", EntityArgumentType.players())
                                .then(literal("get").executes(NumismaticCommand::get))
                                .then(longSubcommand("set", "value", NumismaticCommand::set))
                                .then(longSubcommand("add", "amount", NumismaticCommand.modify(1)))
                                .then(longSubcommand("subtract", "amount", NumismaticCommand.modify(-1)))))
                .then(literal("serverworth").executes(NumismaticCommand::serverWorth)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> longSubcommand(String name, String argName, Command<ServerCommandSource> command) {
        return literal(name).then(argument(argName, LongArgumentType.longArg(0)).executes(command));
    }

    @SuppressWarnings("ConstantConditions")
    private static int serverWorth(CommandContext<ServerCommandSource> context) {
        final var playerManager = context.getSource().getServer().getPlayerManager();

        var onlineUUIDs = playerManager.getPlayerList().stream().map(Entity::getUuid).toList();
        var offlineUUIDs = OfflineDataLookup.savedPlayers().stream().filter(uuid -> !onlineUUIDs.contains(uuid)).toList();

        long serverWorth = 0;
        for (var onlineId : onlineUUIDs) {
            serverWorth += ModComponents.CURRENCY.get(playerManager.getPlayer(onlineId)).getValue();
        }

        for (var offlineId : offlineUUIDs) {
            serverWorth += OfflineDataLookup.get(offlineId).getCompound("cardinal_components").getCompound("numismatic-overhaul:currency").getLong("Value");
        }

        long finalServerWorth = serverWorth;
        context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> server net worth: " + finalServerWorth,
                Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);

        return (int) serverWorth;
    }

    private static int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var value = LongArgumentType.getLong(context, "value");
        final var players = EntityArgumentType.getPlayers(context, "player");

        for (var player : players) {
            //noinspection deprecation
            ModComponents.CURRENCY.get(player).setValue(value);
            context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + " set to: " + value,
                    Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
        }

        return CurrencyConverter.asInt(value);
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var players = EntityArgumentType.getPlayers(context, "player");
        long totalBalance = 0;

        for (var player : players) {
            final long balance = ModComponents.CURRENCY.get(player).getValue();
            totalBalance += balance;

            context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + ": " + balance,
                    Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
        }

        return CurrencyConverter.asInt(totalBalance);
    }

    private static Command<ServerCommandSource> modify(long multiplier) {
        return context -> {
            final var amount = LongArgumentType.getLong(context, "amount");
            final var players = EntityArgumentType.getPlayers(context, "player");

            long lastValue = 0;

            for (var player : players) {
                final var currencyComponent = ModComponents.CURRENCY.get(player);

                currencyComponent.silentModify(amount * multiplier);
                lastValue = currencyComponent.getValue();

                context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + " set to: " + currencyComponent.getValue(),
                        Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
            }

            return CurrencyConverter.asInt(lastValue);
        };
    }
}
