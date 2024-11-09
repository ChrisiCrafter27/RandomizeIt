package de.chrisicrafter.randomizeit.event;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.RandomizerCapability;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.ModMessages;
import de.chrisicrafter.randomizeit.networking.packet.ChangeGameruleS2CPacket;
import de.chrisicrafter.randomizeit.networking.packet.UpdateRandomizerDataS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RandomizeIt.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            GameRules gameRules = level.getGameRules();
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.showDiscoveredMutations, gameRules.getBoolean(ModGameRules.SHOW_DISCOVERED_MUTATIONS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomBlockDrops, gameRules.getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomMobDrops, gameRules.getBoolean(ModGameRules.RANDOM_MOB_DROPS)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomCraftingResult, gameRules.getBoolean(ModGameRules.RANDOM_CRAFTING_RESULT)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.randomChestLoot, gameRules.getBoolean(ModGameRules.RANDOM_CHEST_LOOT)), player);
            ModMessages.sendToPlayer(new ChangeGameruleS2CPacket(GameruleData.staticChestLoot, gameRules.getBoolean(ModGameRules.RANDOM_CHEST_LOOT)), player);
            ModMessages.sendToPlayer(new UpdateRandomizerDataS2CPacket(RandomizerData.getInstance(level, player)), player);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            RandomizerData.getInstance(event.getServer().overworld(), null).doTick(event.getServer().overworld());
            for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                player.getCapability(RandomizerCapability.CAPABILITY).ifPresent(cap -> cap.doTick(event.getServer().overworld()));
            }
        }
        if(event.phase == TickEvent.Phase.END) {
            if(event.getServer().getGameRules().getBoolean(ModGameRules.PLAYER_UNIQUE_DATA)) {
                event.getServer().getPlayerList().getPlayers().forEach(player ->  {
                    player.getCapability(RandomizerCapability.CAPABILITY).ifPresent(cap -> {
                        if(cap.sendData()) ModMessages.sendToPlayer(new UpdateRandomizerDataS2CPacket(cap), player);
                    });
                });
            } else {
                ModMessages.sendToPlayer(new UpdateRandomizerDataS2CPacket(RandomizerData.getInstance(event.getServer().overworld(), null)));
            }
        }
    }

    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof ServerPlayer player && !player.getCapability(RandomizerCapability.CAPABILITY).isPresent()) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "properties"), new RandomizerCapability());
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(RandomizerCapability.CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(RandomizerCapability.CAPABILITY).ifPresent(newStore -> {
                    newStore.copyOf(oldStore);
                });
            });
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerHarvest(PlayerEvent.HarvestCheck event) {
        if(event.getEntity().level() instanceof ServerLevel level && level.getGameRules().getBoolean(ModGameRules.RANDOM_BLOCK_DROPS)
                && level.getGameRules().getBoolean(ModGameRules.IGNORE_TOOL_FOR_RANDOM_BLOCK_DROP)) event.setCanHarvest(true);
    }
}
