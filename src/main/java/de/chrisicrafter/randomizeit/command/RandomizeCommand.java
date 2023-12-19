package de.chrisicrafter.randomizeit.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;

public class RandomizeCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ITEMS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(ForgeRegistries.ITEMS.getKeys(), builder);

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("randomize")
                .requires(context -> context.hasPermission(/*cheat level*/ 2))
                .then(Commands.literal("reset")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.literal("*")
                                .executes(RandomizeCommand::reset))
                        .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                .executes(context -> reset(context, context.getArgument("type", Type.class), true))))
                .then(Commands.literal("get")
                        .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_ITEMS)
                                        .executes(context -> get(context, ResourceLocationArgument.getId(context, "item"), context.getArgument("type", Type.class))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("type", EnumArgument.enumArgument(Type.class))
                                .then(Commands.argument("key", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_ITEMS)
                                        .then(Commands.argument("value", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ITEMS)
                                                .executes(context -> set(context, ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), false))
                                                .then(Commands.literal("replace")
                                                        .executes(context -> set(context, ResourceLocationArgument.getId(context, "key"), ResourceLocationArgument.getId(context, "value"), context.getArgument("type", Type.class), true)))))));
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        reset(context, Type.blockDrops, false);
        reset(context, Type.mobDrops, false);
        reset(context, Type.craftingResults, false);
        reset(context, Type.chestLoots, false);
        context.getSource().sendSuccess(() -> Component.translatable("commands.randomizeit.randomize.reset.all"), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context, Type type, boolean printSuccess) {
        switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel()).resetBlockDrops();
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel()).resetMobDrops();
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel()).resetCraftingResult();
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel()).resetChestLoot();
        }
        if(printSuccess) context.getSource().sendSuccess(() -> Component.translatable("commands.randomizeit.randomize.reset.one", type.toString()), true);
        return 1;
    }

    private static int get(CommandContext<CommandSourceStack> context, ResourceLocation id, Type type) {
        Item key = ForgeRegistries.ITEMS.getValue(id);
        if(key == null || key == Items.AIR) {
            throw new CommandRuntimeException(Component.literal("Not an item: " + id.toString()));
        }
        Item value1 = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel()).blockDropsSource(key);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel()).mobDropsSource(key);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel()).craftingResultSource(key);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel()).chestLootSource(key);
        };
        Item value2 = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForBlock(key, false);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForMob(key, false);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForRecipe(key, false);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel()).getStaticRandomizedItemForLoot(key, false);
        };
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.randomizeit.randomize.get.success",
                type.toString(),
                value1 == null ? "unknown" : Component.translatable(value1.getDescriptionId()),
                Component.translatable(key.getDescriptionId()),
                value2 == null ? "unknown" : Component.translatable(value2.getDescriptionId())), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, ResourceLocation id1, ResourceLocation id2, Type type, boolean replace) {
        Item key = ForgeRegistries.ITEMS.getValue(id1);
        if(key == null || key == Items.AIR) {
            throw new CommandRuntimeException(Component.literal("Not an item: " + id1.toString()));
        }
        Item value = ForgeRegistries.ITEMS.getValue(id2);
        if(value == null || value == Items.AIR) {
            throw new CommandRuntimeException(Component.literal("Not an item: " + id2.toString()));
        }
        Item previous = switch (type) {
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForBlock(key, false);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForMob(key, false);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel()).getRandomizedItemForRecipe(key, false);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel()).getStaticRandomizedItemForLoot(key, false);
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
            case blockDrops -> RandomizerData.getInstance(context.getSource().getLevel()).setBlockDrop(key, value);
            case mobDrops -> RandomizerData.getInstance(context.getSource().getLevel()).setMobDrop(key, value);
            case craftingResults -> RandomizerData.getInstance(context.getSource().getLevel()).setCraftingResult(key, value);
            case chestLoots -> RandomizerData.getInstance(context.getSource().getLevel()).setChestLoot(key, value);
        };
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.randomizeit.randomize.set.success",
                type.toString(),
                Component.translatable(key.getDescriptionId()),
                Component.translatable(value.getDescriptionId())), false);
        return 1;
    }

    enum Type {
        blockDrops,
        mobDrops,
        craftingResults,
        chestLoots,
    }
}
