package de.chrisicrafter.randomizeit;

import com.mojang.logging.LogUtils;
import de.chrisicrafter.randomizeit.data.ModAttachments;
import de.chrisicrafter.randomizeit.command.RandomizeCommand;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.UpdateGameruleS2CPayload;
import de.chrisicrafter.randomizeit.networking.UpdateRandomizerDataPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(RandomizeIt.MOD_ID)
public class RandomizeIt {
    public static final String MOD_ID = "randomizeit";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RandomizeIt() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        assert modEventBus != null;
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloadHandlers);
        ModAttachments.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("COMMON SETUP");
            ModGameRules.register();
        });
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        UpdateRandomizerDataPayload.TYPE,
                        UpdateRandomizerDataPayload.CODEC,
                        UpdateRandomizerDataPayload::handle)
                .playToClient(
                        UpdateGameruleS2CPayload.TYPE,
                        UpdateGameruleS2CPayload.CODEC,
                        UpdateGameruleS2CPayload::handle)
                .optional();
    }

    @SubscribeEvent
    public void addCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(RandomizeCommand.register());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RandomizerData.setInstance(event.getServer());
    }
}
