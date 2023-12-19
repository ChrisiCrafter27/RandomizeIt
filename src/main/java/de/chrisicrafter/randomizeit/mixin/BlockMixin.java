package de.chrisicrafter.randomizeit.mixin;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = Block.class)
public class BlockMixin {

    @Invoker("getDrops")
    public static List<ItemStack> getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack) {
        LootParams.Builder builder1 = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, itemStack).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return state.getDrops(builder1).stream().map(stack -> new ItemStack(RandomizerData.getInstance(level).getRandomizedItemForBlock(stack.getItem()), stack.getCount())).toList();
    }
}
