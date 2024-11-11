package de.chrisicrafter.randomizeit.data;

import de.chrisicrafter.randomizeit.RandomizeIt;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RandomizeIt.MOD_ID);

    public static final Supplier<AttachmentType<RandomizerAttachment>> RANDOMIZER_ATTACHMENT = ATTACHMENT_TYPES.register(
            "randomizer", () -> AttachmentType.builder(RandomizerAttachment::new).serialize(new IAttachmentSerializer<CompoundTag, RandomizerAttachment>() {
                @Override
                public @NotNull RandomizerAttachment read(@NotNull IAttachmentHolder iAttachmentHolder, @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
                    RandomizerAttachment cap = new RandomizerAttachment();
                    cap.deserializeNBT(provider, tag);
                    return cap;
                }
                @Override
                public @Nullable CompoundTag write(@NotNull RandomizerAttachment randomizerAttachment, HolderLookup.@NotNull Provider provider) {
                    return randomizerAttachment.serializeNBT(provider);
                }
            }).copyOnDeath().build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
