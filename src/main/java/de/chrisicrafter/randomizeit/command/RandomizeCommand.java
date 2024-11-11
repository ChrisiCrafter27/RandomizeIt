package de.chrisicrafter.randomizeit.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.Collection;
import java.util.List;

public class RandomizeCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ITEMS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder);

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("randomize")
                .requires(context -> context.hasPermission(/*cheat level*/ 2))
                .then(Commands.literal("reset")
                        .then(Commands.literal("*")
                                .executes(RandomizeCommand::resetAll))
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.literal("*")
                                                .executes(context -> reset(context, EntityArgument.getPlayers(context, "players"))))
                                        .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                                .executes(context -> reset(context, context.getArgument("type", Type.class), EntityArgument.getPlayers(context, "players"), true)))))
                        .then(Commands.literal("server")
                                .then(Commands.literal("*")
                                        .executes(RandomizeCommand::resetServer))
                                .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                        .executes(context -> reset(context, context.getArgument("type", Type.class), true)))))
                .then(Commands.literal("get")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ITEMS)
                                                .executes(context -> get(context, EntityArgument.getPlayer(context, "player"), ResourceLocationArgument.getId(context, "item"), context.getArgument("type", Type.class)))))))
                        .then(Commands.literal("server").then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_ITEMS)
                                        .executes(context -> get(context, null, ResourceLocationArgument.getId(context, "item"), context.getArgument("type", Type.class)))))))
                .then(Commands.literal("set")
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                                .then(Commands.argument("key", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_ITEMS)
                                                        .then(Commands.argument("value", ResourceLocationArgument.id())
                                                                .suggests(SUGGEST_ITEMS)
                                                                .executes(context -> set(context, EntityArgument.getPlayers(context, "players"), ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), false))
                                                                .then(Commands.literal("replace")
                                                                        .executes(context -> set(context, EntityArgument.getPlayers(context, "players"), ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), true))))))))
                        .then(Commands.literal("server")
                                .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                        .then(Commands.argument("key", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ITEMS)
                                                .then(Commands.argument("value", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_ITEMS)
                                                        .executes(context -> set(context, ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), false))
                                                        .then(Commands.literal("replace")
                                                                .executes(context -> set(context, ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), true))))))));
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        reset(context, Type.blockDrops, false);
        reset(context, Type.mobDrops, false);
        reset(context, Type.craftingResults, false);
        reset(context, Type.chestLoots, false);
        for(ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            reset(context, Type.blockDrops, List.of(player), false);
            reset(context, Type.mobDrops, List.of(player), false);
            reset(context, Type.craftingResults, List.of(player), false);
            reset(context, Type.chestLoots, List.of(player), false);
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.randomizeit.randomize.reset.all.server"), true);
        return 1;
    }

    private static int resetServer(CommandContext<CommandSourceStack> context) {
        reset(context, Type.blockDrops, false);
        reset(context, Type.mobDrops, false);
        reset(context, Type.craftingResults, false);
        reset(context, Type.chestLoots, false);
        context.getSource().sendSuccess(() -> Component.translatable("commands.randomizeit.randomize.reset.all.server"), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        for(ServerPlayer player : players) {
            reset(context, Type.blockDrops, List.of(player), false);
            reset(context, Type.mobDrops, List.of(player), false);
            reset(context, Type.craftingResults, List.of(player), false);
            reset(context, Type.chestLoots, List.of(player), false);
        }
        context.getSource().sendSuccess(() -> switch (players.size()) {
            case 0 -> Component.translatable("commands.randomizeit.randomize.reset.all.player.zero");
            case 1 -> Component.translatable("commands.randomizeit.randomize.reset.all.player.one", players.iterator().next().getName());
            default -> Component.translatable("commands.randomizeit.randomize.reset.all.player.multiple", players.size());
        }, true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context, Type type, boolean printSuccess) {
        switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).resetBlockDrops();
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).resetMobDrops();
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), null).resetCraftingResult();
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), null).resetChestLoot();
        }
        if(printSuccess) context.getSource().sendSuccess(() -> Component.translatable("commands.randomizeit.randomize.reset.one.server", type.toString()), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context, Type type, Collection<ServerPlayer> players, boolean printSuccess) {
        for(ServerPlayer player : players) {
            switch (type) {
                case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).resetBlockDrops();
                case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).resetMobDrops();
                case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), player).resetCraftingResult();
                case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), player).resetChestLoot();
            }
        }
        if(printSuccess) {
            context.getSource().sendSuccess(() -> switch (players.size()) {
                case 0 -> Component.translatable("commands.randomizeit.randomize.reset.one.player.zero", type.toString());
                case 1 -> Component.translatable("commands.randomizeit.randomize.reset.one.player.one", type.toString(), players.iterator().next().getName());
                default -> Component.translatable("commands.randomizeit.randomize.reset.one.player.multiple", type.toString(), players.size());
            }, true);
        }
        return 1;
    }

    private static int get(CommandContext<CommandSourceStack> context, ServerPlayer player, ResourceLocation id, Type type) throws CommandSyntaxException {
        Item key = BuiltInRegistries.ITEM.get(id);
        if(key == Items.AIR) {
            throw new SimpleCommandExceptionType(Component.literal("Not an item: " + id.toString())).create();
        }
        Item value1 = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).blockDropsSource(key);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).mobDropsSource(key);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), player).craftingResultSource(key);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), player).chestLootSource(key);
        };
        Item value2 = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForBlock(key, null, context.getSource().getLevel(), false);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForMob(key, null, context.getSource().getLevel(), false);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForRecipe(key, null, context.getSource().getLevel(), false);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), player).getStaticRandomizedItemForLoot(key, null, context.getSource().getLevel(), false);
        };
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.randomizeit.randomize.get.success",
                type.toString(),
                value1 == null ? "unknown" : Component.translatable(value1.getDescriptionId()),
                Component.translatable(key.getDescriptionId()),
                value2 == null ? "unknown" : Component.translatable(value2.getDescriptionId()),
                player == null ? "server" : player.getName()), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, ResourceLocation id1, ResourceLocation id2, Type type, boolean replace) throws CommandSyntaxException {
        Item key = BuiltInRegistries.ITEM.get(id1);
        if(key == Items.AIR) {
            throw new SimpleCommandExceptionType(Component.literal("Not an item: " + id1.toString())).create();
        }
        Item value = BuiltInRegistries.ITEM.get(id2);
        if(value == Items.AIR) {
            throw new SimpleCommandExceptionType(Component.literal("Not an item: " + id2.toString())).create();
        }
        Item previous = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).getRandomizedItemForBlock(key, null, context.getSource().getLevel(), false);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).getRandomizedItemForMob(key, null, context.getSource().getLevel(), false);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), null).getRandomizedItemForRecipe(key, null, context.getSource().getLevel(), false);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), null).getStaticRandomizedItemForLoot(key, null, context.getSource().getLevel(), false);
        };
        if(previous != null && !replace) {
            context.getSource().sendFailure(Component.translatable(
                    "commands.randomizeit.randomize.set.failure",
                    type.toString(),
                    Component.translatable(key.getDescriptionId()),
                    Component.translatable(previous.getDescriptionId())));
            return 0;
        }
        switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).setBlockDrop(key, value);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), null).setMobDrop(key, value);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), null).setCraftingResult(key, value);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), null).setChestLoot(key, value);
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.randomizeit.randomize.set.success.server",
                type.toString(),
                Component.translatable(key.getDescriptionId()),
                Component.translatable(value.getDescriptionId())), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, ResourceLocation id1, ResourceLocation id2, Type type, boolean replace) throws CommandSyntaxException {
        Item key = BuiltInRegistries.ITEM.get(id1);
        if(key == Items.AIR) {
            throw new SimpleCommandExceptionType(Component.literal("Not an item: " + id1.toString())).create();
        }
        Item value = BuiltInRegistries.ITEM.get(id2);
        if(value == Items.AIR) {
            throw new SimpleCommandExceptionType(Component.literal("Not an item: " + id2.toString())).create();
        }
        int count = 0;
        for(ServerPlayer player : players) {
            Item previous = switch (type) {
                case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForBlock(key, null, context.getSource().getLevel(), false);
                case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForMob(key, null, context.getSource().getLevel(), false);
                case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), player).getRandomizedItemForRecipe(key, null, context.getSource().getLevel(), false);
                case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), player).getStaticRandomizedItemForLoot(key, null, context.getSource().getLevel(), false);
            };
            if(previous != null && !replace) {
                context.getSource().sendFailure(Component.translatable(
                        "commands.randomizeit.randomize.set.failure",
                        type.toString(),
                        Component.translatable(key.getDescriptionId()),
                        Component.translatable(previous.getDescriptionId())));
                continue;
            }
            switch (type) {
                case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).setBlockDrop(key, value);
                case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel(), player).setMobDrop(key, value);
                case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel(), player).setCraftingResult(key, value);
                case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel(), player).setChestLoot(key, value);
            }
            count++;
        }
        final int finalCount = count;
        switch(finalCount) {
            case 0 -> context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.randomizeit.randomize.set.success.player.zero",
                    type.toString(),
                    Component.translatable(key.getDescriptionId()),
                    Component.translatable(value.getDescriptionId())), false);
            case 1 -> context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.randomizeit.randomize.set.success.player.one",
                    type.toString(),
                    players.iterator().next().getName(),
                    Component.translatable(key.getDescriptionId()),
                    Component.translatable(value.getDescriptionId())), false);
            default -> context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.randomizeit.randomize.set.success.player.multiple",
                    type.toString(),
                    finalCount,
                    Component.translatable(key.getDescriptionId()),
                    Component.translatable(value.getDescriptionId())), false);
        }
        return 1;
    }

    enum Type {
        blockDrops,
        mobDrops,
        craftingResults,
        chestLoots,
    }
}
