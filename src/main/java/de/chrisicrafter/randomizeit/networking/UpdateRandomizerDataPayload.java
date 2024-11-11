package de.chrisicrafter.randomizeit.networking;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.data.RandomizerData;
import de.chrisicrafter.randomizeit.data.client.ClientRandomizerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class UpdateRandomizerDataPayload implements CustomPacketPayloadWithHandler {
    public static final CustomPacketPayload.Type<UpdateRandomizerDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RandomizeIt.MOD_ID, "update_randomizer"));
    public static final StreamCodec<FriendlyByteBuf, UpdateRandomizerDataPayload> CODEC = new SimpleStreamCodec<>(UpdateRandomizerDataPayload::new, UpdateRandomizerDataPayload::toBytes);

    private final CompoundTag nbt;

    public UpdateRandomizerDataPayload(RandomizerData data) {
        nbt = new CompoundTag();
        data.save(nbt);
    }

    public UpdateRandomizerDataPayload(FriendlyByteBuf buf) {
        nbt = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void handle(IPayloadContext iPayloadContext) {
        ClientRandomizerData.setInstance(ClientRandomizerData.load(nbt));
    }

    @Override
    public @NotNull Type<UpdateRandomizerDataPayload> type() {
        return TYPE;
    }
}
