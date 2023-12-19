package de.chrisicrafter.randomizeit.networking.packet;

import de.chrisicrafter.randomizeit.data.client.GameruleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ChangeGameruleS2CPacket {
    private final GameruleData data;
    private final boolean enabled;

    public ChangeGameruleS2CPacket(GameruleData data, boolean enabled) {
        this.data = data;
        this.enabled = enabled;
    }

    public ChangeGameruleS2CPacket(FriendlyByteBuf buf) {
        data = buf.readEnum(GameruleData.class);
        enabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(data);
        buf.writeBoolean(enabled);
    }

    public void handle(CustomPayloadEvent.Context context) {
        GameruleData.valueOf(data.name()).enabled = enabled;
    }
}
