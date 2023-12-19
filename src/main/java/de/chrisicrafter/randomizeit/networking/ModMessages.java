package de.chrisicrafter.skillscreenapi.common.networking;

import de.chrisicrafter.skillscreenapi.SkillScreenApi;
import de.chrisicrafter.skillscreenapi.common.networking.packet.*;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
        SimpleChannel net = ChannelBuilder.named(new ResourceLocation(SkillScreenApi.MOD_ID, "messages"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(SeenSkillsC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SeenSkillsC2SPacket::new)
                .encoder(SeenSkillsC2SPacket::toBytes)
                .consumerMainThread(SeenSkillsC2SPacket::handle)
                .add();

        net.messageBuilder(UnlockSkillC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UnlockSkillC2SPacket::new)
                .encoder(UnlockSkillC2SPacket::toBytes)
                .consumerMainThread(UnlockSkillC2SPacket::handle)
                .add();

        net.messageBuilder(SelectSkillsTabS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SelectSkillsTabS2CPacket::new)
                .encoder(SelectSkillsTabS2CPacket::toBytes)
                .consumerMainThread(SelectSkillsTabS2CPacket::handle)
                .add();

        net.messageBuilder(UpdateSkillsTabS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateSkillsTabS2CPacket::new)
                .encoder(UpdateSkillsTabS2CPacket::toBytes)
                .consumerMainThread(UpdateSkillsTabS2CPacket::handle)
                .add();

        net.messageBuilder(ShowToastS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ShowToastS2CPacket::new)
                .encoder(ShowToastS2CPacket::toBytes)
                .consumerMainThread(ShowToastS2CPacket::handle)
                .add();

    }

    public static <MSG> void sendToPlayer(MSG message) {
        INSTANCE.send(message, PacketDistributor.ALL.noArg());
    }

    public static <MSG> void sendToPlayer(MSG message, ResourceKey<Level> dimension) {
        INSTANCE.send(message, PacketDistributor.DIMENSION.with(dimension));
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToServer(MSG message, Connection connection) {
        INSTANCE.send(message, connection);
    }
}
