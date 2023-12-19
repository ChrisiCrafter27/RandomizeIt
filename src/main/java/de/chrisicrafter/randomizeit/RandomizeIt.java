package de.chrisicrafter.randomizeit;

import com.mojang.logging.LogUtils;
import de.chrisicrafter.randomizeit.command.RandomizeCommand;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.gamerule.ModGameRules;
import de.chrisicrafter.randomizeit.networking.ModMessages;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RandomizeIt.MOD_ID)
public class RandomizeIt {
    public static final String MOD_ID = "randomizeit";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RandomizeIt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("COMMON SETUP");
            ModMessages.register();
            ModGameRules.register();
        });
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
