package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerEntity.class)
public interface ContainerEntityMixin extends Container, MenuProvider {
    @Shadow
    Level level();

    @Shadow
    ResourceLocation getLootTable();

    @Inject(method = "unpackChestVehicleLootTable", at = @At("TAIL"))
    default void unpackChestVehicleLootTable(Player player, CallbackInfo info) {
        MinecraftServer minecraftserver = this.level().getServer();
        if (this.getLootTable() != null && minecraftserver != null && level().getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)) {
            for(int i = 0; i < getContainerSize(); i++) {
                setItem(i, new ItemStack(RandomizerData.getInstance((ServerLevel) level()).getRandomizedItemForLoot(getItem(i).getItem()), getItem(i).getCount()));
            }
        }
    }
}
