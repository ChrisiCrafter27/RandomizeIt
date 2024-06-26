package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.advancements.CriteriaTriggers;
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

import java.util.HashMap;
import java.util.Map;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerMixin extends AbstractMinecart implements ContainerEntity {
    @Unique private final Map<Item, Item> randomizeIt$map = new HashMap<>();

    @Shadow public abstract @NotNull ItemStack getItem(int p_38218_);

    protected AbstractMinecartContainerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void unpackChestVehicleLootTable(Player player) {
        MinecraftServer minecraftserver = this.level().getServer();
        if (this.getLootTable() != null && minecraftserver != null) {
            LootTable loottable = minecraftserver.getLootData().getLootTable(this.getLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
            }

            this.setLootTable(null);
            LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());

            lootparams$builder.withParameter(LootContextParams.KILLER_ENTITY, (AbstractMinecartContainer) (Object) this);
            if (player != null) {
                lootparams$builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }

            loottable.fill(this, lootparams$builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());

            if (level().getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)) {
                for(int i = 0; i < getContainerSize(); i++) {
                    setItem(i, new ItemStack(randomizeIt$getRandomizedItem((ServerLevel) level(), player, getItem(i).getItem()), getItem(i).getCount()));
                }
            }
        }
    }

    @Unique
    private Item randomizeIt$getRandomizedItem(ServerLevel level, Player player, Item item) {
        if(level.getGameRules().getBoolean(ModGameRules.STATIC_CHEST_LOOT)) {
            return RandomizerData.getInstance(level, player).getStaticRandomizedItemForLoot(item, true);
        } else {
            if(!randomizeIt$map.containsKey(item)) randomizeIt$map.put(item, RandomizerData.getInstance(level, player).getUniqueRandomizedItemForLoot());
            return randomizeIt$map.get(item);
        }
    }
}
