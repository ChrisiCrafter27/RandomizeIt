package de.chrisicrafter.randomizeit.networking;

import de.chrisicrafter.randomizeit.RandomizeIt;
import de.chrisicrafter.randomizeit.networking.packet.ChangeGameruleS2CPacket;
import de.chrisicrafter.randomizeit.networking.packet.UpdateRandomizerDataS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = ChannelBuilder.named(new ResourceLocation(RandomizeIt.MOD_ID, "messages"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(UpdateRandomizerDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateRandomizerDataS2CPacket::new)
                .encoder(UpdateRandomizerDataS2CPacket::toBytes)
                .consumerMainThread(UpdateRandomizerDataS2CPacket::handle)
                .add();

        net.messageBuilder(ChangeGameruleS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ChangeGameruleS2CPacket::new)
                .encoder(ChangeGameruleS2CPacket::toBytes)
                .consumerMainThread(ChangeGameruleS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message) {
        INSTANCE.send(message, PacketDistributor.ALL.noArg());
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }
}
