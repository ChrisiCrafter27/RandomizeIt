package de.chrisicrafter.randomizeit.event;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RandomizeIt.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if(GameruleData.showDiscoveredMutations.enabled) {
            RandomizerData.getInstance().ifPresent(data -> {
                if(GameruleData.randomBlockDrops.enabled) {
                    Item blockItem = data.getRandomizedItemForBlockIfSet(event.getItemStack().getItem());
                    if(blockItem != null) {
                        event.getToolTip().add(Component.literal("Obtained by block drop: ").append(blockItem.getName(new ItemStack(blockItem))));
                    }
                }
                if(GameruleData.randomMobDrops.enabled) {
                    Item entityItem = data.getRandomizedItemForBlockIfSet(event.getItemStack().getItem());
                    if(entityItem != null) {
                        event.getToolTip().add(Component.literal("Obtained by mob drop: ").append(entityItem.getName(new ItemStack(entityItem))));
                    }
                }
            });
        }
    }
}
