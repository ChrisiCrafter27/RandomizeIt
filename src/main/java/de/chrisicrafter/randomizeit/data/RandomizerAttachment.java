package de.chrisicrafter.randomizeit.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RandomizerAttachment extends RandomizerData implements INBTSerializable<CompoundTag> {
    public RandomizerAttachment() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public RandomizerAttachment(HashMap<Item, Item> blockDrops, HashMap<Item, Item> entityDrops, HashMap<Item, Item> craftingResult, HashMap<Item, Item> chestLoot) {
        super(blockDrops, entityDrops, craftingResult, chestLoot);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        return save(new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag nbt) {
        copyOf(RandomizerData.load(nbt));
    }

    public void copyOf(RandomizerData data) {
        blockDrops.clear();
        blockDrops.putAll(data.blockDrops);
        entityDrops.clear();
        entityDrops.putAll(data.entityDrops);
        craftingResult.clear();
        craftingResult.putAll(data.craftingResult);
        chestLoot.clear();
        chestLoot.putAll(data.chestLoot);
    }
}
