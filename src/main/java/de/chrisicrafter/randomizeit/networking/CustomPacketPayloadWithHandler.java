package de.chrisicrafter.randomizeit.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface CustomPacketPayloadWithHandler extends CustomPacketPayload {
    void handle(IPayloadContext payloadContext);
}
