package de.chrisicrafter.randomizeit.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@AutoRegisterCapability
public class RandomizerCapability extends RandomizerData implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<RandomizerCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public RandomizerCapability() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
    }

    public RandomizerCapability(HashMap<Item, Item> blockDrops, HashMap<Item, Item> entityDrops, HashMap<Item, Item> craftingResult, HashMap<Item, Item> chestLoot, int time) {
        super(blockDrops, entityDrops, craftingResult, chestLoot, time);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CAPABILITY) return LazyOptional.of(() -> this).cast();
        else return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return save(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
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
