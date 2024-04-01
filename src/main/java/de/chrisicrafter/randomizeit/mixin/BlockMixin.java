package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerCapability;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements ItemLike, IForgeBlock {
    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> info) {
        if(level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)) info.setReturnValue(info.getReturnValue().stream().map(stack -> new ItemStack(RandomizerData.getInstance(level, null).getRandomizedItemForBlock(stack.getItem(), true), stack.getCount())).toList());
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getDrops(BlockState p_49882_, Level p_49883_, BlockPos p_49884_, BlockEntity p_49885_, Entity p_49886_, ItemStack p_49887_, boolean dropXp, CallbackInfo info) {
        if (p_49883_ instanceof ServerLevel) {
            randomizeIt$getDrops(Block.getDrops(p_49882_, (ServerLevel)p_49883_, p_49884_, p_49885_, p_49886_, p_49887_), (p_49886_ instanceof ServerPlayer player) ? player : null, (ServerLevel)p_49883_).forEach((p_49944_) -> {
                Block.popResource(p_49883_, p_49884_, p_49944_);
            });
            p_49882_.spawnAfterBreak((ServerLevel)p_49883_, p_49884_, p_49887_, dropXp);
        }
        info.cancel();
    }

    @Unique
    private static List<ItemStack> randomizeIt$getDrops(List<ItemStack> items, @Nullable ServerPlayer player, ServerLevel level) {
        if(level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS))
            return items.stream().map(stack -> new ItemStack(RandomizerData.getInstance(level, player).getRandomizedItemForBlock(stack.getItem(), true), stack.getCount())).toList();
        else return items;
    }
}
