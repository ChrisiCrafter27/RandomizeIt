package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements ItemLike, IBlockExtension {
    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> info) {
        if(level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)) {
            if(!level.getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA)) {
                LootParams.Builder lootparams$builder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY) //TODO
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
                List<ItemStack> returnValue = state.getDrops(lootparams$builder);

                info.setReturnValue(randomizeIt$getDrops(returnValue, null, level));
            } else info.setReturnValue(List.of());
        }
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack item, CallbackInfoReturnable<List<ItemStack>> info) {
        if(level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)) {
            if(!level.getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA)) {
                LootParams.Builder lootparams$builder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, item)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
                List<ItemStack> returnValue = state.getDrops(lootparams$builder);

                info.setReturnValue(randomizeIt$getDrops(returnValue, null, level));
            } else if(entity instanceof ServerPlayer player) {
                LootParams.Builder lootparams$builder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, item)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
                List<ItemStack> returnValue = state.getDrops(lootparams$builder);

                info.setReturnValue(randomizeIt$getDrops(returnValue, player, level));
            } else info.setReturnValue(List.of());
        }
    }

    @Unique
    private static List<ItemStack> randomizeIt$getDrops(List<ItemStack> items, @Nullable ServerPlayer player, ServerLevel level) {
        return items.stream().map(stack -> new ItemStack(RandomizerData.getInstance(level, player).getRandomizedItemForBlock(stack.getItem(), player, level, true), stack.getCount())).toList();
    }
}
