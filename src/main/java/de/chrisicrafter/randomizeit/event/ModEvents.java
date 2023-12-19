package de.chrisicrafter.randomizeit.event;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.ModMessages;
import de.chrisicrafter.randomizeit.networking.packet.ChangeGameruleS2CPacket;
import de.chrisicrafter.randomizeit.networking.packet.UpdateRandomizerDataS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RandomizeIt.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.showDiscoveredMutations, event.getLevel().getGameRules().getBoolean(ModGameRules.SHOW_DISCOVERED_MUTATIONS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomBlockDrops, event.getLevel().getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomMobDrops, event.getLevel().getGameRules().getBoolean(ModGameRules.RANDOM_MOB_DROPS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomCraftingResult, event.getLevel().getGameRules().getBoolean(ModGameRules.RANDOM_CRAFTING_RESULT)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomChestLoot, event.getLevel().getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.staticChestLoot, event.getLevel().getGameRules().getBoolean(ModGameRules.RANDOM_CHEST_LOOT)), player);
            ModMessages.sendToPlayer(new UpdateRandomizerDataS2CPacket(RandomizerData.getInstance(level)), player);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) RandomizerData.getInstance(event.getServer().overworld()).doTick(event.getServer().overworld());
    }
}
