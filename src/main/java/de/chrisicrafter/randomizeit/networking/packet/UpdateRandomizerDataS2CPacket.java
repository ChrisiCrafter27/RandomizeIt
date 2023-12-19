package de.chrisicrafter.randomizeit.networking.packet;

import de.chrisicrafter.skillscreenapi.SkillScreenApi;
import de.chrisicrafter.skillscreenapi.common.data.PlayerSkillsProvider;
import de.chrisicrafter.skillscreenapi.common.skills.SkillHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.NoSuchElementException;

public class SeenSkillsC2SPacket {
    private final Action action;
    private final ResourceLocation tab;

    public SeenSkillsC2SPacket(Action action, ResourceLocation tab) {
        this.action = action;
        this.tab = tab;
    }

    public static SeenSkillsC2SPacket openedTab(SkillHolder holder) {
        return holder == null ? new SeenSkillsC2SPacket(Action.OPEN_NULL, null) : new SeenSkillsC2SPacket(Action.OPENED_TAB, holder.id());
    }

    public static SeenSkillsC2SPacket closedScreen() {
        return new SeenSkillsC2SPacket(Action.CLOSED_SCREEN, null);
    }

    public SeenSkillsC2SPacket(FriendlyByteBuf buf) {
        action = buf.readEnum(Action.class);
        if (action == Action.OPENED_TAB) {
            tab = buf.readResourceLocation();
        } else {
            tab = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        if (action == Action.OPENED_TAB) {
            buf.writeResourceLocation(tab);
        }
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            SkillScreenApi.LOGGER.info("Server: skill tab");
            if (getAction() == Action.OPENED_TAB || getAction() == Action.OPEN_NULL) {
                SkillScreenApi.LOGGER.info("Server: open skill tab");
                ResourceLocation id = getTab();
                SkillHolder holder = SkillScreenApi.getSkillManager().get(id);
                context.getSender().getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new).setSelectedTab(holder, context.getSender());
            }
        });
    }

    public Action getAction() {
        return this.action;
    }

    public ResourceLocation getTab() {
        return this.tab;
    }

    public enum Action {
        OPENED_TAB,
        CLOSED_SCREEN,
        OPEN_NULL
    }
}
