package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cross-loader networking, built on Architectury API's {@link NetworkManager} (see
 * https://docs.architectury.dev/api/networking) - one {@code registerReceiver}/
 * {@code sendToPlayer} call here reaches Fabric, Forge and NeoForge alike, same as the
 * {@code registry} package's {@code DeferredRegister} wrappers.
 *
 * <p>Worked example: {@link com.example.examplemod.block.ExampleBlock}'s interact handler
 * increments {@link com.example.examplemod.block.ExampleBlockEntity}'s click counter
 * server-side, then this class notifies the clicking player's client of the new value -
 * the common "server changed some data, tell the client" pattern. This is deliberately
 * just a point-to-point notification, not full initial-chunk-load sync (that's
 * {@code BlockEntity#getUpdatePacket()}/{@code #getUpdateTag(...)}, out of scope for this
 * worked example).
 *
 * <p><b>Registering an S2C receiver from common code that runs on both physical sides is a
 * real trap</b> - caught by the {@code fabric/src/gametest/} GameTests actually booting a
 * server (see README.md's "Testing" section); {@code chiseledBuild} never runs anything, so
 * it can't catch this. On Fabric, {@code NetworkManager.registerReceiver(Side.S2C, ...)}
 * throws {@code AbstractMethodError} the moment it actually runs on a <em>dedicated server</em>
 * - a receiver is only ever invoked client-side, but the call to register one apparently still
 * needs to run somewhere the client-side networking internals actually exist (confirmed
 * against a real, still-open upstream report:
 * https://github.com/architectury/architectury-api/issues/518). So this is split in two:
 * {@link #init()} (called from both physical sides, via {@link com.example.examplemod.ExampleMod#init()})
 * only registers the payload <em>type</em> ({@code registerS2CPayloadType} - needed so the
 * server can encode and send one at all), and {@link #initClient()} (called only from
 * {@link com.example.examplemod.client.ExampleModClient#init()}, which by construction never
 * runs on a dedicated server) registers the actual receiver. Follow this same split for any
 * S2C packet you add; a C2S packet (client sends, server receives) has the opposite shape -
 * register {@code C2S}'s receiver from {@link #init()} instead, never from the client-only
 * class.
 *
 * <p>This is also the one spot in this template that hits a genuine Minecraft <em>version</em>
 * difference of real substance: 1.20.5 replaced the old raw-{@code FriendlyByteBuf},
 * {@code ResourceLocation}-keyed packet model with typed {@code CustomPacketPayload}
 * records encoded via {@code StreamCodec} (https://docs.neoforged.net/docs/networking/payload/).
 * Nothing loader-specific here - Architectury API's {@link NetworkManager} covers both
 * shapes identically across Fabric/Forge/NeoForge - so this is a Stonecutter {@code //? if}
 * split, not another abstraction, same reasoning as
 * {@link com.example.examplemod.registry.ModSounds}, which hits the same
 * {@code ResourceLocation}/{@code Identifier} rename {@link #id} handles below. Every
 * version-specific type below is fully-qualified inline rather than imported, since (as in
 * {@code ExampleBlock}/{@code ModSounds}) some of these classes don't exist at all on the
 * other side of the split - an unconditional import would break compilation for whichever
 * versions don't have it, even if unused. The pre-1.20.5 packet model needs no equivalent
 * common-side registration at all - only {@link #initClient()} has anything to do there.
 */
public final class ModNetworking {
    private ModNetworking() {
    }

    /** Called once from {@link com.example.examplemod.ExampleMod#init()} - runs on both physical sides. */
    public static void init() {
        register();
    }

    /** Called once from {@link com.example.examplemod.client.ExampleModClient#init()} - client-only, never runs on a dedicated server. */
    public static void initClient() {
        registerClientReceiver();
    }

    //? if >=1.20.5 {
    public record CounterSyncPacket(BlockPos pos, int counter) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
        public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<CounterSyncPacket> TYPE =
                new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(id("counter_sync"));

        public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CounterSyncPacket> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        BlockPos.STREAM_CODEC, CounterSyncPacket::pos,
                        net.minecraft.network.codec.ByteBufCodecs.VAR_INT, CounterSyncPacket::counter,
                        CounterSyncPacket::new);

        @Override
        public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<CounterSyncPacket> type() {
            return TYPE;
        }
    }

    private static void register() {
        NetworkManager.registerS2CPayloadType(CounterSyncPacket.TYPE, CounterSyncPacket.STREAM_CODEC);
    }

    private static void registerClientReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CounterSyncPacket.TYPE, CounterSyncPacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> onCounterSync(payload.pos(), payload.counter())));
    }

    /** Notifies one player that the block entity at {@code pos} now has this counter value. */
    public static void sendCounterSync(ServerPlayer player, BlockPos pos, int counter) {
        NetworkManager.sendToPlayer(player, new CounterSyncPacket(pos, counter));
    }
    //?} else {
    /*
    public static final net.minecraft.resources.ResourceLocation COUNTER_SYNC_ID = id("counter_sync");

    // Nothing to register common-side pre-1.20.5 - sending a raw packet needs no prior
    // "payload type" registration, unlike the CustomPacketPayload model above.
    private static void register() {
    }

    private static void registerClientReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, COUNTER_SYNC_ID, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            int counter = buf.readInt();
            context.queue(() -> onCounterSync(pos, counter));
        });
    }

    /// Notifies one player that the block entity at {@code pos} now has this counter value.
    public static void sendCounterSync(ServerPlayer player, BlockPos pos, int counter) {
        net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(counter);
        NetworkManager.sendToPlayer(player, COUNTER_SYNC_ID, buf);
    }
    */
    //?}

    private static void onCounterSync(BlockPos pos, int counter) {
        ExampleMod.LOGGER.info("[client] block at {} now shows counter={}", pos, counter);
    }

    // Same ResourceLocation -> Identifier rename ModSounds#id hits - see its doc comment.
    //? if >=1.21.11 {
    /*
    private static net.minecraft.resources.Identifier id(String path) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, path);
    }
    */
    //?} else if >=1.21 {
    private static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, path);
    }
    //?} else {
    /*
    private static net.minecraft.resources.ResourceLocation id(String path) {
        return new net.minecraft.resources.ResourceLocation(ExampleMod.MOD_ID, path);
    }
    */
    //?}
}
