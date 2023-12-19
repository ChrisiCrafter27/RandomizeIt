package de.chrisicrafter.randomizeit.data;

import de.chrisicrafter.randomizeit.networking.ModMessages;
import de.chrisicrafter.randomizeit.networking.packet.UpdateRandomizerDataS2CPacket;
import de.chrisicrafter.randomizeit.utils.MapUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomizerData extends SavedData {
    private static RandomizerData instance;
    private final HashMap<Item, Item> blockDrops;
    private final HashMap<Item, Item> entityDrops;
    private final HashMap<Item, Item> craftingResult;
    private final HashMap<Item, Item> chestLoot;

    public static Factory<RandomizerData> factory() {
        return new Factory<>(RandomizerData::new, RandomizerData::load, DataFixTypes.LEVEL);
    }

    private RandomizerData() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public RandomizerData(HashMap<Item, Item> blockDrops, HashMap<Item, Item> entityDrops, HashMap<Item, Item> craftingResult, HashMap<Item, Item> chestLoot) {
        this.blockDrops = blockDrops;
        this.entityDrops = entityDrops;
        this.craftingResult = craftingResult;
        this.chestLoot = chestLoot;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        int size1 = blockDrops.size();
        tag.putInt("block_drops", size1);

        int i1 = 0;
        for(Map.Entry<Item, Item> entry : blockDrops.entrySet()) {
            tag.putString("block_drop_k" + i1, ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            tag.putString("block_drop_v" + i1, ForgeRegistries.ITEMS.getKey(entry.getValue()).toString());
            i1++;
        }

        int size2 = entityDrops.size();
        tag.putInt("entity_drops", size2);

        int i2 = 0;
        for(Map.Entry<Item, Item> entry : entityDrops.entrySet()) {
            tag.putString("entity_drop_k" + i2, ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            tag.putString("entity_drop_v" + i2, ForgeRegistries.ITEMS.getKey(entry.getValue()).toString());
            i2++;
        }

        int size3 = craftingResult.size();
        tag.putInt("crafting_results", size3);

        int i3 = 0;
        for(Map.Entry<Item, Item> entry : craftingResult.entrySet()) {
            tag.putString("crafting_result_k" + i3, ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            tag.putString("crafting_result_v" + i3, ForgeRegistries.ITEMS.getKey(entry.getValue()).toString());
            i3++;
        }

        int size4 = chestLoot.size();
        tag.putInt("chest_loots", size4);

        int i4 = 0;
        for(Map.Entry<Item, Item> entry : chestLoot.entrySet()) {
            tag.putString("chest_loot_k" + i4, ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            tag.putString("chest_loot_v" + i4, ForgeRegistries.ITEMS.getKey(entry.getValue()).toString());
            i4++;
        }

        return tag;
    }

    public static RandomizerData load(CompoundTag tag) {
        HashMap<Item, Item> blockDrops = new HashMap<>();

        int size1 = tag.getInt("block_drops");
        for(int i = 0; i < size1; i++) {
            blockDrops.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("block_drop_k" + i))), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("block_drop_v" + i))));
        }

        HashMap<Item, Item> entityDrops = new HashMap<>();

        int size2 = tag.getInt("entity_drops");
        for(int i = 0; i < size2; i++) {
            entityDrops.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("entity_drop_k" + i))), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("entity_drop_v" + i))));
        }

        HashMap<Item, Item> craftingResult = new HashMap<>();

        int size3 = tag.getInt("crafting_results");
        for(int i = 0; i < size3; i++) {
            craftingResult.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("crafting_result_k" + i))), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("crafting_result_v" + i))));
        }

        HashMap<Item, Item> chestLoots = new HashMap<>();

        int size4 = tag.getInt("chest_loots");
        for(int i = 0; i < size4; i++) {
            chestLoots.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("chest_loot_k" + i))), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("chest_loot_v" + i))));
        }

        return new RandomizerData(blockDrops, entityDrops, craftingResult, chestLoots);
    }

    @Override
    public void setDirty() {
        super.setDirty();
        ModMessages.sendToPlayer(new UpdateRandomizerDataS2CPacket(this));
    }

    public static void setInstance(MinecraftServer server) {
        instance = server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(RandomizerData.factory(), "randomizer_data");
    }

    public static RandomizerData getInstance(ServerLevel level) {
        if(instance == null) instance = level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(RandomizerData.factory(), "randomizer_data");
        return instance;
    }

    public Item getRandomizedItemForBlock(Item key, boolean computeIfAbsent) {
        if(!blockDrops.containsKey(key) && computeIfAbsent) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(block -> !blockDrops.containsValue(block)).toList();
            blockDrops.put(key, list.get(RandomSource.create().nextInt(0, list.size())));
            setDirty();
        }
        return blockDrops.get(key);
    }

    public Item getRandomizedItemForMob(Item key, boolean computeIfAbsent) {
        if(!entityDrops.containsKey(key) && computeIfAbsent) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(entity -> !entityDrops.containsValue(entity)).toList();
            entityDrops.put(key, list.get(RandomSource.create().nextInt(0, list.size())));
            setDirty();
        }
        return entityDrops.get(key);
    }

    public Item getRandomizedItemForRecipe(Item key, boolean computeIfAbsent) {
        if(!craftingResult.containsKey(key) && computeIfAbsent) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(entity -> !craftingResult.containsValue(entity)).toList();
            craftingResult.put(key, list.get(RandomSource.create().nextInt(0, list.size())));
            setDirty();
        }
        return craftingResult.get(key);
    }

    public Item getStaticRandomizedItemForLoot(Item key, boolean computeIfAbsent) {
        if(!chestLoot.containsKey(key) && computeIfAbsent) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(entity -> !chestLoot.containsValue(entity)).toList();
            chestLoot.put(key, list.get(RandomSource.create().nextInt(0, list.size())));
            setDirty();
        }
        return chestLoot.get(key);
    }

    public Item getUniqueRandomizedItemForLoot() {
        List<Item> list = ForgeRegistries.ITEMS.getValues().stream().toList();
        return list.get(RandomSource.create().nextInt(0, list.size()));
    }

    public Item blockDropsSource(Item item) {
        return MapUtils.getKey(blockDrops, item);
    }

    public Item mobDropsSource(Item item) {
        return MapUtils.getKey(entityDrops, item);
    }

    public Item craftingResultSource(Item item) {
        return MapUtils.getKey(craftingResult, item);
    }

    public Item chestLootSource(Item item) {
        return MapUtils.getKey(chestLoot, item);
    }

    public void resetBlockDrops() {
        blockDrops.clear();
        setDirty();
    }

    public void resetMobDrops() {
        entityDrops.clear();
        setDirty();
    }

    public void resetCraftingResult() {
        craftingResult.clear();
        setDirty();
    }

    public void resetChestLoot() {
        chestLoot.clear();
        setDirty();
    }

    public void setBlockDrop(Item key, Item value) {
        blockDrops.put(key, value);
        setDirty();
    }

    public void setMobDrop(Item key, Item value) {
        entityDrops.put(key, value);
        setDirty();
    }

    public void setCraftingResult(Item key, Item value) {
        craftingResult.put(key, value);
        setDirty();
    }

    public void setChestLoot(Item key, Item value) {
        chestLoot.put(key, value);
        setDirty();
    }
}
