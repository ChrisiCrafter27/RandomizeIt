package de.chrisicrafter.randomizeit.data.client;

import de.chrisicrafter.randomizeit.utils.MapUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Optional;

public class ClientRandomizerData {
    private static ClientRandomizerData instance;
    private final HashMap<Item, Item> blockDrops;
    private final HashMap<Item, Item> entityDrops;
    private final HashMap<Item, Item> craftingResult;
    private final HashMap<Item, Item> chestLoot;

    public ClientRandomizerData(HashMap<Item, Item> blockDrops, HashMap<Item, Item> entityDrops, HashMap<Item, Item> craftingResult, HashMap<Item, Item> chestLoot) {
        this.blockDrops = blockDrops;
        this.entityDrops = entityDrops;
        this.craftingResult = craftingResult;
        this.chestLoot = chestLoot;
    }

    public static ClientRandomizerData load(CompoundTag tag) {
        HashMap<Item, Item> blockDrops = new HashMap<>();

        int size1 = tag.getInt("block_drops");
        for(int i = 0; i < size1; i++) {
            blockDrops.put(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("block_drop_k" + i))), BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("block_drop_v" + i))));
        }

        HashMap<Item, Item> entityDrops = new HashMap<>();

        int size2 = tag.getInt("entity_drops");
        for(int i = 0; i < size2; i++) {
            entityDrops.put(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("entity_drop_k" + i))), BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("entity_drop_v" + i))));
        }

        HashMap<Item, Item> craftingResult = new HashMap<>();

        int size3 = tag.getInt("crafting_results");
        for(int i = 0; i < size3; i++) {
            craftingResult.put(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("crafting_result_k" + i))), BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("crafting_result_v" + i))));
        }

        HashMap<Item, Item> chestLoots = new HashMap<>();

        int size4 = tag.getInt("chest_loots");
        for(int i = 0; i < size4; i++) {
            chestLoots.put(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("chest_loot_k" + i))), BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("chest_loot_v" + i))));
        }

        return new ClientRandomizerData(blockDrops, entityDrops, craftingResult, chestLoots);
    }

    public static void setInstance(ClientRandomizerData data) {
        instance = data;
    }

    public static Optional<ClientRandomizerData> getInstance() {
        return Optional.ofNullable(instance);
    }

    public Item getBlockSourceOfItem(Item value) {
        return MapUtils.getKey(blockDrops, value);
    }

    public Item getEntitySourceOfItem(Item value) {
        return MapUtils.getKey(entityDrops, value);
    }

    public Item getRecipeSourceOfItem(Item value) {
        return MapUtils.getKey(craftingResult, value);
    }

    public Item getLootSourceOfItem(Item value) {
        return MapUtils.getKey(chestLoot, value);
    }
}
