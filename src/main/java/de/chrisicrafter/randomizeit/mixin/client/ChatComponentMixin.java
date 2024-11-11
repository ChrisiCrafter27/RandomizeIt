package de.chrisicrafter.randomizeit.mixin.client;

import de.chrisicrafter.randomizeit.ChatItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int drawItems(GuiGraphics guiGraphics, Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color) {
        ChatItemRenderer.renderItemForMessage(guiGraphics, formattedCharSequence, x, y, color);
        return guiGraphics.drawString(font, formattedCharSequence, x, y, color);
    }
}
