package de.chrisicrafter.randomizeit.networking.packet;

import de.chrisicrafter.randomizeit.data.RandomizerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class UpdateRandomizerDataS2CPacket {
    private final CompoundTag nbt;

    public UpdateRandomizerDataS2CPacket(RandomizerData data) {
        nbt = new CompoundTag();
        data.save(nbt);
    }

    public UpdateRandomizerDataS2CPacket(FriendlyByteBuf buf) {
        nbt = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    public void handle(CustomPayloadEvent.Context context) {
        RandomizerData.setInstance(RandomizerData.load(nbt));
    }
}
