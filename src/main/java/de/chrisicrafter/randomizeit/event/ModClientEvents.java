package de.chrisicrafter.randomizeit.event;

import com.mojang.datafixers.util.Either;
import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.client.ClientRandomizerData;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = RandomizeIt.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        if(GameruleData.showDiscoveredMutations.enabled) {
            ClientRandomizerData.getInstance().ifPresent(data -> {
                if(GameruleData.randomBlockDrops.enabled) {
                    Item blockItem = data.getBlockSourceOfItem(event.getItemStack().getItem());
                    if(blockItem != null) {
                        event.getTooltipElements().add(Either.left(Component
                                .literal("Obtained by block drop: ")
                                .append(blockItem.getName(new ItemStack(blockItem)))
                                .withStyle(ChatFormatting.GRAY)));
                    }
                }
                if(GameruleData.randomMobDrops.enabled) {
                    Item entityItem = data.getEntitySourceOfItem(event.getItemStack().getItem());
                    if(entityItem != null) {
                        event.getTooltipElements().add(Either.left(Component
                                .literal("Obtained by mob drop: ")
                                .append(entityItem.getName(new ItemStack(entityItem)))
                                .withStyle(ChatFormatting.GRAY)));
                    }
                }
                if(GameruleData.randomCraftingResult.enabled) {
                    Item craftingItem = data.getRecipeSourceOfItem(event.getItemStack().getItem());
                    if(craftingItem != null) {
                        event.getTooltipElements().add(Either.left(Component
                                .literal("Obtained by crafting: ")
                                .append(craftingItem.getName(new ItemStack(craftingItem)))
                                .withStyle(ChatFormatting.GRAY)));
                    }
                }
                if(GameruleData.randomChestLoot.enabled && GameruleData.staticChestLoot.enabled) {
                    Item lootItem = data.getLootSourceOfItem(event.getItemStack().getItem());
                    if(lootItem != null) {
                        event.getTooltipElements().add(Either.left(Component
                                .literal("Obtained by looting: ")
                                .append(lootItem.getName(new ItemStack(lootItem)))
                                .withStyle(ChatFormatting.GRAY)));
                    }
                }
            });
        }
    }
}
