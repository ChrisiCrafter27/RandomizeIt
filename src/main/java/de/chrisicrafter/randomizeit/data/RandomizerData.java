package de.chrisicrafter.randomizeit.data;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.PlayUiSoundS2CPayload;
import de.chrisicrafter.randomizeit.utils.MapUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RandomizerData extends SavedData {
    private static final SoundEvent COMMON = SoundEvents.PLAYER_LEVELUP;
    private static final SoundEvent UNCOMMON = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "discovery.uncommon"));
    private static final SoundEvent RARE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "discovery.rare"));
    private static final SoundEvent EPIC = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;

    private static RandomizerData instance;
    protected final HashMap<Item, Item> blockDrops;
    protected final HashMap<Item, Item> entityDrops;
    protected final HashMap<Item, Item> craftingResult;
    protected final HashMap<Item, Item> chestLoot;
    protected boolean sendData;
    private static int requestedSound = -1;

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
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        return save(tag);
    }

    public @NotNull CompoundTag save(CompoundTag tag) {
        int size1 = blockDrops.size();
        tag.putInt("block_drops", size1);

        int i1 = 0;
        for(Map.Entry<Item, Item> entry : blockDrops.entrySet()) {
            tag.putString("block_drop_k" + i1, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            tag.putString("block_drop_v" + i1, BuiltInRegistries.ITEM.getKey(entry.getValue()).toString());
            i1++;
        }

        int size2 = entityDrops.size();
        tag.putInt("entity_drops", size2);

        int i2 = 0;
        for(Map.Entry<Item, Item> entry : entityDrops.entrySet()) {
            tag.putString("entity_drop_k" + i2, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            tag.putString("entity_drop_v" + i2, BuiltInRegistries.ITEM.getKey(entry.getValue()).toString());
            i2++;
        }

        int size3 = craftingResult.size();
        tag.putInt("crafting_results", size3);

        int i3 = 0;
        for(Map.Entry<Item, Item> entry : craftingResult.entrySet()) {
            tag.putString("crafting_result_k" + i3, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            tag.putString("crafting_result_v" + i3, BuiltInRegistries.ITEM.getKey(entry.getValue()).toString());
            i3++;
        }

        int size4 = chestLoot.size();
        tag.putInt("chest_loots", size4);

        int i4 = 0;
        for(Map.Entry<Item, Item> entry : chestLoot.entrySet()) {
            tag.putString("chest_loot_k" + i4, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            tag.putString("chest_loot_v" + i4, BuiltInRegistries.ITEM.getKey(entry.getValue()).toString());
            i4++;
        }

        return tag;
    }

    public static RandomizerData load(CompoundTag tag, HolderLookup.Provider provider) {
        return load(tag);
    }

    public static RandomizerData load(CompoundTag tag) {
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

        return new RandomizerData(blockDrops, entityDrops, craftingResult, chestLoots);
    }

    @Override
    public void setDirty() {
        super.setDirty();
        sendData = true;
    }

    public boolean sendData() {
        if(sendData) {
            sendData = false;
            return true;
        } else return false;
    }

    public static void setInstance(MinecraftServer server) {
        instance = server.overworld().getDataStorage().computeIfAbsent(RandomizerData.factory(), "randomizer_data");
    }

    public static RandomizerData getInstance(ServerLevel level, Entity entity) {
        if(instance == null) instance = level.getServer().overworld().getDataStorage().computeIfAbsent(RandomizerData.factory(), "randomizer_data");
        if(level.getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA) && entity instanceof ServerPlayer player) {
            return player.getData(ModAttachments.RANDOMIZER_ATTACHMENT);
        }
        return instance;
    }

    public static void doTick() {
        if(requestedSound >= 0) {
            SoundEvent soundEvent = switch (requestedSound) {
                case 0 -> COMMON;
                case 1 -> UNCOMMON;
                case 2 -> RARE;
                case 3 -> EPIC;
                default -> throw new IllegalStateException("Unexpected value: " + requestedSound);
            };
            PacketDistributor.sendToAllPlayers(new PlayUiSoundS2CPayload(soundEvent));
            requestedSound = -1;
        }
    }

    public Item getRandomizedItemForBlock(Item key, ServerPlayer player, ServerLevel level, boolean computeIfAbsent) {
        if(!blockDrops.containsKey(key) && computeIfAbsent) {
            List<Item> list = BuiltInRegistries.ITEM.stream().filter(item -> !(item instanceof GameMasterBlockItem)).filter(item -> item.isEnabled(level.enabledFeatures())).filter(item -> !blockDrops.containsValue(item)).toList();
            Item value = list.get(RandomSource.create().nextInt(list.size()));
            blockDrops.put(key, value);
            int rarityInt = toInt(value.components().get(DataComponents.RARITY));
            if(level.getGameRules().getInt(ModGameRules.DISCOVERY_ANNOUNCEMENT_RARITY_LEVEL) <= rarityInt) {
                if(requestedSound < rarityInt) requestedSound = rarityInt;
                level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                                "message.discovered.block",
                                player != null ? Objects.requireNonNull(player.getDisplayName()).copy().withStyle(ChatFormatting.WHITE) : Component.literal("Server").withStyle(ChatFormatting.WHITE),
                                new ItemStack(key).getDisplayName(),
                                new ItemStack(value).getDisplayName())
                        , false);
            }
            setDirty();
        }
        return blockDrops.get(key);
    }

    public Item getRandomizedItemForMob(Item key, ServerPlayer player, ServerLevel level, boolean computeIfAbsent) {
        if(!entityDrops.containsKey(key) && computeIfAbsent) {
            List<Item> list = BuiltInRegistries.ITEM.stream().filter(item -> !(item instanceof GameMasterBlockItem)).filter(item -> item.isEnabled(level.enabledFeatures())).filter(item -> !entityDrops.containsValue(item)).toList();
            Item value = list.get(RandomSource.create().nextInt(list.size()));
            entityDrops.put(key, value);
            int rarityInt = toInt(value.components().get(DataComponents.RARITY));
            if(level.getGameRules().getInt(ModGameRules.DISCOVERY_ANNOUNCEMENT_RARITY_LEVEL) <= rarityInt) {
                if(requestedSound < rarityInt) requestedSound = rarityInt;
                level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                                "message.discovered.mob",
                                player != null ? Objects.requireNonNull(player.getDisplayName()).copy().withStyle(ChatFormatting.WHITE) : Component.literal("Server").withStyle(ChatFormatting.WHITE),
                                new ItemStack(key).getDisplayName(),
                                new ItemStack(value).getDisplayName())
                        .withStyle(ChatFormatting.GRAY), false);
            }
            setDirty();
        }
        return entityDrops.get(key);
    }

    public Item getRandomizedItemForRecipe(Item key, ServerPlayer player, ServerLevel level, boolean computeIfAbsent) {
        if(!craftingResult.containsKey(key) && computeIfAbsent) {
            List<Item> list = BuiltInRegistries.ITEM.stream().filter(item -> !(item instanceof GameMasterBlockItem)).filter(item -> item.isEnabled(level.enabledFeatures())).filter(item -> !craftingResult.containsValue(item)).toList();
            Item value = list.get(RandomSource.create().nextInt(list.size()));
            craftingResult.put(key, value);
            int rarityInt = toInt(value.components().get(DataComponents.RARITY));
            if(level.getGameRules().getInt(ModGameRules.DISCOVERY_ANNOUNCEMENT_RARITY_LEVEL) <= rarityInt) {
                if(requestedSound < rarityInt) requestedSound = rarityInt;
                level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                                "message.discovered.craft",
                                player != null ? Objects.requireNonNull(player.getDisplayName()).copy().withStyle(ChatFormatting.WHITE) : Component.literal("Server").withStyle(ChatFormatting.WHITE),
                                new ItemStack(key).getDisplayName(),
                                new ItemStack(value).getDisplayName())
                        .withStyle(ChatFormatting.GRAY), false);
            }
            setDirty();
        }
        return craftingResult.get(key);
    }

    public Item getStaticRandomizedItemForLoot(Item key, ServerPlayer player, ServerLevel level, boolean computeIfAbsent) {
        if(!chestLoot.containsKey(key) && computeIfAbsent) {
            List<Item> list = BuiltInRegistries.ITEM.stream().filter(item -> !(item instanceof GameMasterBlockItem)).filter(item -> item.isEnabled(level.enabledFeatures())).filter(item -> !chestLoot.containsValue(item)).toList();
            Item value = list.get(RandomSource.create().nextInt(list.size()));
            chestLoot.put(key, value);
            int rarityInt = toInt(value.components().get(DataComponents.RARITY));
            if(level.getGameRules().getInt(ModGameRules.DISCOVERY_ANNOUNCEMENT_RARITY_LEVEL) <= rarityInt) {
                if(requestedSound < rarityInt) requestedSound = rarityInt;
                level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                        "message.discovered.loot",
                                player != null ? Objects.requireNonNull(player.getDisplayName()).copy().withStyle(ChatFormatting.WHITE) : Component.literal("Server").withStyle(ChatFormatting.WHITE),
                                new ItemStack(key).getDisplayName(),
                                new ItemStack(value).getDisplayName())
                        .withStyle(ChatFormatting.GRAY), false);
            }
            setDirty();
        }
        return chestLoot.get(key);
    }

    public Item getUniqueRandomizedItemForLoot(ServerLevel level) {
        List<Item> list = BuiltInRegistries.ITEM.stream().filter(item -> !(item instanceof GameMasterBlockItem)).filter(item -> item.isEnabled(level.enabledFeatures())).toList();
        return list.get(RandomSource.create().nextInt(list.size()));
    }

    private static int toInt(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0;
            case UNCOMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case null -> 0;
        };
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
