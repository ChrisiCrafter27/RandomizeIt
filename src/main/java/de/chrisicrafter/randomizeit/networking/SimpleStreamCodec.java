package de.chrisicrafter.randomizeit.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleStreamCodec<T> implements StreamCodec<FriendlyByteBuf, T> {
    private final Function<FriendlyByteBuf, T> decoder;
    private final BiConsumer<T, FriendlyByteBuf> encoder;

    public SimpleStreamCodec(Function<FriendlyByteBuf, T> decoder, BiConsumer<T, FriendlyByteBuf> encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public @NotNull T decode(@NotNull FriendlyByteBuf friendlyByteBuf) {
        return decoder.apply(friendlyByteBuf);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf friendlyByteBuf, @NotNull T t) {
        encoder.accept(t, friendlyByteBuf);
    }
}
