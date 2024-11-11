package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin extends BaseContainerBlockEntity implements RandomizableContainer {
    @Unique private final Map<Item, Item> randomizeIt$map = new HashMap<>();

    @Shadow public abstract void setItem(int slot, @NotNull ItemStack item);
    @Shadow public abstract @NotNull ItemStack getItem(int slot);

    protected RandomizableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void unpackLootTable(Player player) {
        if (level != null && level.getServer() != null && level.getServer().getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)) {
            Level level = this.getLevel();
            BlockPos blockpos = this.getBlockPos();
            ResourceKey<LootTable> resourcekey = this.getLootTable();
            if (resourcekey != null && level != null && level.getServer() != null) {
                LootTable loottable = level.getServer().reloadableRegistries().getLootTable(resourcekey);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, resourcekey);
                }

                this.setLootTable(null);
                LootParams.Builder lootparams$builder = new LootParams.Builder((ServerLevel)level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos));
                if (player != null) {
                    lootparams$builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
                }

                loottable.fill(this, lootparams$builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());

                NonNullList<ItemStack> items = getItems();
                items.replaceAll(item -> {
                    if(item.is(Items.AIR) || item.getCount() == 0) return item;
                    return new ItemStack(randomizeIt$getRandomizedItem((ServerLevel) this.level, player, item.getItem()), item.getCount());
                });
                setItems(items);
            }
        } else RandomizableContainer.super.unpackLootTable(player);
    }

    @Unique
    private Item randomizeIt$getRandomizedItem(ServerLevel level, Player player, Item item) {
        if(level.getGameRules().getBoolean(ModGameRules.STATIC_CHEST_LOOT)) {
            return RandomizerData.getInstance(level, player).getStaticRandomizedItemForLoot(item, level, true);
        } else {
            if(!randomizeIt$map.containsKey(item)) randomizeIt$map.put(item, RandomizerData.getInstance(level, player).getUniqueRandomizedItemForLoot(level));
            return randomizeIt$map.get(item);
        }
    }
}
