package de.chrisicrafter.randomizeit.networking;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.client.GameruleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class UpdateGameruleS2CPayload implements CustomPacketPayloadWithHandler {
    public static final CustomPacketPayload.Type<UpdateGameruleS2CPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "update_gamerule"));
    public static final StreamCodec<FriendlyByteBuf, UpdateGameruleS2CPayload> CODEC = new SimpleStreamCodec<>(UpdateGameruleS2CPayload::new, UpdateGameruleS2CPayload::toBytes);

    private final GameruleData data;
    private final boolean enabled;

    public UpdateGameruleS2CPayload(GameruleData data, boolean enabled) {
        this.data = data;
        this.enabled = enabled;
    }

    public UpdateGameruleS2CPayload(FriendlyByteBuf buf) {
        data = buf.readEnum(GameruleData.class);
        enabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(data);
        buf.writeBoolean(enabled);
    }

    @Override
    public void handle(IPayloadContext context) {
        GameruleData.valueOf(data.name()).enabled = enabled;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
