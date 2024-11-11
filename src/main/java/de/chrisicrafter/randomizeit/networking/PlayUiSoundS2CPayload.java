package de.chrisicrafter.randomizeit.networking;

import de.chrisicrafter.randomizeit.RandomizeIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PlayUiSoundS2CPayload implements CustomPacketPayloadWithHandler {
    public static final Type<PlayUiSoundS2CPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "play_ui_sound"));
    public static final StreamCodec<FriendlyByteBuf, PlayUiSoundS2CPayload> CODEC = new SimpleStreamCodec<>(PlayUiSoundS2CPayload::new, PlayUiSoundS2CPayload::toBytes);

    private final SoundEvent soundEvent;

    public PlayUiSoundS2CPayload(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    public PlayUiSoundS2CPayload(FriendlyByteBuf buf) {
        soundEvent = SoundEvent.DIRECT_STREAM_CODEC.decode(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        SoundEvent.DIRECT_STREAM_CODEC.encode(buf, soundEvent);
    }

    @Override
    public void handle(IPayloadContext context) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
