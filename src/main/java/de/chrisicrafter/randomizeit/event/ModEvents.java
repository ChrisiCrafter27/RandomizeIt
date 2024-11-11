package de.chrisicrafter.randomizeit.event;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.ModAttachments;
import de.chrisicrafter.randomizeit.data.RandomizerAttachment;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.UpdateGameruleS2CPayload;
import de.chrisicrafter.randomizeit.networking.UpdateRandomizerDataPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = RandomizeIt.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            GameRules gameRules = level.getGameRules();
            PacketDistributor.sendToPlayer(player,
                    new UpdateGameruleS2CPayload(GameruleData.showDiscoveredMutations, gameRules.getBoolean(ModGameRules.SHOW_DISCOVERED_MUTATIONS)),
                    new UpdateGameruleS2CPayload(GameruleData.randomBlockDrops, gameRules.getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)),
                    new UpdateGameruleS2CPayload(GameruleData.randomMobDrops, gameRules.getBoolean(ModGameRules.RANDOM_MOB_DROPS)),
                    new UpdateGameruleS2CPayload(GameruleData.randomCraftingResult, gameRules.getBoolean(ModGameRules.RANDOM_CRAFTING_RESULT)),
                    new UpdateGameruleS2CPayload(GameruleData.randomChestLoot, gameRules.getBoolean(ModGameRules.RANDOM_CHEST_LOOT)),
                    new UpdateGameruleS2CPayload(GameruleData.staticChestLoot, gameRules.getBoolean(ModGameRules.RANDOM_CHEST_LOOT)),
                    new UpdateRandomizerDataPayload(RandomizerData.getInstance(level, player)));
        }
    }

    @SubscribeEvent
    public static void onTickPre(ServerTickEvent.Pre event) {
        RandomizerData.getInstance(event.getServer().overworld(), null).doTick(event.getServer().overworld());
        for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            RandomizerAttachment cap = player.getData(ModAttachments.RANDOMIZER_ATTACHMENT);
            cap.doTick(event.getServer().overworld());
        }
    }

    @SubscribeEvent
    public static void onTickPost(ServerTickEvent.Post event) {
        if(event.getServer().getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA)) {
            event.getServer().getPlayerList().getPlayers().forEach(player ->  {
                RandomizerAttachment cap = player.getData(ModAttachments.RANDOMIZER_ATTACHMENT);
                if(cap.sendData()) PacketDistributor.sendToPlayer(player, new UpdateRandomizerDataPayload(cap));
            });
        } else {
            PacketDistributor.sendToAllPlayers(new UpdateRandomizerDataPayload(RandomizerData.getInstance(event.getServer().overworld(), null)));
        }
    }

    @SubscribeEvent
    public static void onPlayerHarvest(PlayerEvent.HarvestCheck event) {
        if(event.getEntity().level() instanceof ServerLevel level && level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)
                && level.getGameRules().getBoolean(ModGameRules.IGNORE_TOOL_FOR_RANDOM_BLOCK_DROP)) event.setCanHarvest(true);
    }
}
