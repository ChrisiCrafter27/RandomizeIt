package de.chrisicrafter.randomizeit;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ChatItemRenderer {
    public static float alphaValue = 1F;

    @OnlyIn(Dist.CLIENT)
    public static void renderItemForMessage(GuiGraphics guiGraphics, FormattedCharSequence sequence, float x, float y, int color) {
        Minecraft mc = Minecraft.getInstance();

        StringBuilder before = new StringBuilder();

        int halfSpace = mc.font.width(" ") / 2;

        sequence.accept((counter_, style, character) -> {
            String sofar = before.toString();
            if (sofar.endsWith("   ")) {
                render(mc, guiGraphics, sofar.substring(0, sofar.length() - 3), character == ' ' ? 0 : -halfSpace, x, y, style, color);
            }
            before.append((char) character);
            return true;
        });
    }

    public static MutableComponent createStackComponent(MutableComponent component) {
        Style style = component.getStyle();
        MutableComponent out = Component.literal("  ");
        out.setStyle(style);
        return out.append(component);
    }

    @OnlyIn(Dist.CLIENT)
    private static void render(Minecraft mc, GuiGraphics guiGraphics, String before, float extraShift, float x, float y, Style style, int color) {
        float a = (color >> 24 & 255) / 255.0F;

        PoseStack pose = guiGraphics.pose();

        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
            HoverEvent.ItemStackInfo contents = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);

            ItemStack stack = contents != null ? contents.getItemStack() : ItemStack.EMPTY;

            if (stack.isEmpty())
                stack = new ItemStack(Blocks.BARRIER); //For invalid icon

            float shift = mc.font.width(before) + extraShift;

            if (a > 0) {
                alphaValue = a;

                guiGraphics.pose().pushPose();

                guiGraphics.pose().mulPose(pose.last().pose());

                guiGraphics.pose().translate(shift + x, y, 0);
                guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
                guiGraphics.renderItem(stack, 0, 0);
                guiGraphics.pose().popPose();

                alphaValue = 1F;
            }
        }
    }
}
