package de.chrisicrafter.randomizeit.gamerule;

import de.chrisicrafter.randomizeit.data.client.GameruleData;
import de.chrisicrafter.randomizeit.networking.ModMessages;
import de.chrisicrafter.randomizeit.networking.packet.ChangeGameruleS2CPacket;
import net.minecraft.world.level.GameRules;

import java.util.HashMap;

public class ModGameRules {
    private static final HashMap<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = new HashMap<>();

    public static final GameRules.Key<GameRules.BooleanValue> SHOW_DISCOVERED_MUTATIONS = register("showDiscoveredMutations", GameRules.Category.MISC, GameRules.BooleanValue.create(true,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.showDiscoveredMutations, enabled.get()))));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_BLOCK_DROPS = register("randomBlockDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomBlockDrops, enabled.get()))));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_MOB_DROPS = register("randomMobDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomMobDrops, enabled.get()))));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_CRAFTING_RESULT = register("randomCraftingResult", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomCraftingResult, enabled.get()))));
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_CHEST_LOOT = register("randomChestLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomChestLoot, enabled.get()))));
    public static final GameRules.Key<GameRules.BooleanValue> STATIC_CHEST_LOOT = register("staticChestLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(false,
            (server, enabled) -> ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.staticChestLoot, enabled.get()))));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String id, GameRules.Category category, GameRules.Type<?> value) {
        GameRules.Key<T> key = new GameRules.Key<>(id, category);
        GAME_RULE_TYPES.put(key, value);
        return key;
    }

    public static void register() {
        GAME_RULE_TYPES.forEach((key, value) -> {
            GameRules.register(key.toString(), key.getCategory(), value);
        });
    }
}
