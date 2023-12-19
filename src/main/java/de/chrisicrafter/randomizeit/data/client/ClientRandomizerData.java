package de.chrisicrafter.randomizeit.data;

import de.chrisicrafter.randomizeit.RandomizeIt;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RandomizerData extends SavedData {
    private static RandomizerData instance;
    private final HashMap<Item, Item> blockDrops;
    private final HashMap<Item, Item> entityDrops;

    public static Factory<RandomizerData> factory() {
        return new Factory<>(RandomizerData::new, RandomizerData::load, DataFixTypes.LEVEL);
    }

    private RandomizerData() {
        this(new HashMap<>(), new HashMap<>());
    }

    public RandomizerData(HashMap<Item, Item> blockDrops, HashMap<Item, Item> entityDrops) {
        this.blockDrops = blockDrops;
        this.entityDrops = entityDrops;
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

        int size2 = blockDrops.size();
        tag.putInt("entity_drops", size2);

        int i2 = 0;
        for(Map.Entry<Item, Item> entry : blockDrops.entrySet()) {
            tag.putString("entity_drop_k" + i2, ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
            tag.putString("entity_drop_v" + i2, ForgeRegistries.ITEMS.getKey(entry.getValue()).toString());
            i2++;
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
            blockDrops.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("entity_drop_k" + i))), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("entity_drop_v" + i))));
        }

        return new RandomizerData(blockDrops, entityDrops);
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

    @OnlyIn(Dist.CLIENT)
    public static void setInstance(RandomizerData data) {
        instance = data;
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<RandomizerData> getInstance() {
        return Optional.ofNullable(instance);
    }

    public Item getRandomizedItemForBlock(Item key) {
        RandomizeIt.LOGGER.info("I am: " + this);
        if(!blockDrops.containsKey(key)) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(block -> !blockDrops.containsValue(block)).toList();
            blockDrops.put(key, list.get(RandomSource.create().nextInt(0, list.size())));
            setDirty();
        }
        return blockDrops.get(key);
    }

    public Item getRandomizedItemForEntity(Item key) {
        RandomizeIt.LOGGER.info("I am: " + this);
        if(!entityDrops.containsKey(key)) {
            List<Item> list = ForgeRegistries.ITEMS.getValues().stream().filter(entity -> !entityDrops.containsValue(entity)).toList();
            Item value = list.get(RandomSource.create().nextInt(0, list.size()));
            entityDrops.put(key, value);
            setDirty();
        }
        return entityDrops.get(key);
    }

    public Item getBlockSourceOfItem(Item value) {
        return MapUtils.getKey(blockDrops, value);
    }

    public Item getEntitySourceOfItem(Item value) {
        return MapUtils.getKey(entityDrops, value);
    }
}
