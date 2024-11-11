package de.chrisicrafter.randomizeit.mixin.client;

import de.chrisicrafter.randomizeit.ChatItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.renderer.ItemBlockRenderTypes.getChunkRenderType;

@Mixin(ItemBlockRenderTypes.class)
public abstract class ItemBlockRendererTypesMixin {
    @Inject(method = "getRenderType(Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void overrideRenderType(BlockState state, boolean isGlas, CallbackInfoReturnable<RenderType> cir) {
        if (ChatItemRenderer.alphaValue != 1.0F) {
            if (!Minecraft.useShaderTransparency()) {
                cir.setReturnValue(Sheets.cutoutBlockSheet());
            } else {
                RenderType rendertype = getChunkRenderType(state);
                cir.setReturnValue(rendertype == RenderType.translucent() ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet());
            }
        }
    }
}
