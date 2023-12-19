package de.chrisicrafter.randomizeit.gamerule;

import de.chrisicrafter.randomizeit.data.client.GameruleData;
import de.chrisicrafter.randomizeit.networking.ModMessages;
import de.chrisicrafter.randomizeit.networking.packet.ChangeGameruleS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

import java.util.HashMap;

public class ModGameRules {
    private static final HashMap<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = new HashMap<>();

    public static final GameRules.Key<GameRules.BooleanValue> SHOW_DISCOVERED_MUTATIONS = register("showDiscoveredMutations", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.showDiscoveredMutations, enabled.get()))));

    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_BLOCK_DROPS = register("randomBlockDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> {
        ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomBlockDrops, enabled.get()));
        if(server.getGameRules().getBoolean(ModGameRules.ANNOUNCE_RANDOMIZER_TOGGLES)) server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal("[RandomizeIt] " + (enabled.get() ? "enabled" : "disabled") + " randomBlockDrops").withStyle(ChatFormatting.GRAY)));
    }));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_MOB_DROPS = register("randomMobDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> {
        ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomMobDrops, enabled.get()));
        if(server.getGameRules().getBoolean(ModGameRules.ANNOUNCE_RANDOMIZER_TOGGLES)) server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal("[RandomizeIt] " + (enabled.get() ? "enabled" : "disabled") + " randomMobDrops").withStyle(ChatFormatting.GRAY)));
    }));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_CRAFTING_RESULT = register("randomCraftingResult", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> {
        ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomCraftingResult, enabled.get()));
        if(server.getGameRules().getBoolean(ModGameRules.ANNOUNCE_RANDOMIZER_TOGGLES)) server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal("[RandomizeIt] " + (enabled.get() ? "enabled" : "disabled") + " randomCraftingResult").withStyle(ChatFormatting.GRAY)));
    }));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_CHEST_LOOT = register("randomChestLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> {
        ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomChestLoot, enabled.get()));
        if(server.getGameRules().getBoolean(ModGameRules.ANNOUNCE_RANDOMIZER_TOGGLES)) server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal("[RandomizeIt] " + (enabled.get() ? "enabled" : "disabled") + " randomChestLoot").withStyle(ChatFormatting.GRAY)));
    }));
    public static final GameRules.Key<GameRules.BooleanValue> STATIC_CHEST_LOOT = register("staticChestLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(false));

    public static final GameRules.Key<GameRules.IntegerValue> RANDOM_RANDOMIZER_TOGGLE_INTERVAL = register("randomRandomizerToggleInterval", GameRules.Category.UPDATES, GameRules.IntegerValue.create(0,
            ((server, value) -> {if(value.get() < 0) server.getGameRules().getRule(ModGameRules.RANDOM_RANDOMIZER_TOGGLE_INTERVAL).set(0, server);})));
    public static final GameRules.Key<GameRules.BooleanValue> ANNOUNCE_RANDOMIZER_TOGGLES = register("announceRandomizerToggle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String id, GameRules.Category category, GameRules.Type<?> value) {
        GameRules.Key<T> key = new GameRules.Key<>(id, category);
        GAME_RULE_TYPES.put(key, value);
        return key;
    }

    public static void register() {
        GAME_RULE_TYPES.forEach((key, value) -> GameRules.register(key.toString(), key.getCategory(), value));
    }
}
