package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerMixin extends AbstractMinecart implements ContainerEntity {
    @Unique private final Map<Item, Item> randomizeIt$map = new HashMap<>();

    @Shadow public abstract @NotNull ItemStack getItem(int p_38218_);

    @Shadow public abstract long getContainerLootTableSeed();

    @Shadow @Nullable public abstract ResourceKey<LootTable> getContainerLootTable();

    @Shadow public abstract void setContainerLootTable(@org.jetbrains.annotations.Nullable ResourceKey<LootTable> p_331410_);

    protected AbstractMinecartContainerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void unpackChestVehicleLootTable(Player player) {
        MinecraftServer minecraftserver = this.level().getServer();
        if (this.getContainerLootTable() != null && minecraftserver != null) {
            LootTable loottable = minecraftserver.reloadableRegistries().getLootTable(this.getContainerLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getContainerLootTable());
            }

            this.setContainerLootTable(null);
            LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());

            lootparams$builder.withParameter(LootContextParams.ATTACKING_ENTITY, (AbstractMinecartContainer) (Object) this);
            if (player != null) {
                lootparams$builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }

            loottable.fill(this, lootparams$builder.create(LootContextParamSets.CHEST), this.getContainerLootTableSeed());

            if (minecraftserver.getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)) {
                for(int i = 0; i < getContainerSize(); i++) {
                    setItem(i, new ItemStack(randomizeIt$getRandomizedItem((ServerLevel) level(), (ServerPlayer) player, getItem(i).getItem()), getItem(i).getCount()));
                }
            }
        }
    }

    @Unique
    private Item randomizeIt$getRandomizedItem(ServerLevel level, ServerPlayer player, Item item) {
        if(level.getGameRules().getBoolean(ModGameRules.STATIC_CHEST_LOOT)) {
            return RandomizerData.getInstance(level, player).getStaticRandomizedItemForLoot(item, player, level, true);
        } else {
            if(!randomizeIt$map.containsKey(item)) randomizeIt$map.put(item, RandomizerData.getInstance(level, player).getUniqueRandomizedItemForLoot(level));
            return randomizeIt$map.get(item);
        }
    }
}
