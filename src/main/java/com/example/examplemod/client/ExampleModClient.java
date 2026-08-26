package com.example.examplemod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client-only registration (renderers, render types, ...), written once here and called
 * from each loader's own client entry point ({@code ExampleModFabricClient},
 * {@code ExampleModForgeClient}, {@code ExampleModNeoForgeClient}) instead of duplicated
 * three times - Architectury API's client registries are already loader-agnostic (e.g.
 * {@code dev.architectury.registry.client.rendering.RenderTypeRegistry}/
 * {@code BlockEntityRendererRegistry}, or {@code .level.entity.EntityRendererRegistry} for
 * entities); only the entry point that calls them differs per loader, the same as
 * {@link com.example.examplemod.ExampleMod#init()} is per-loader for the common entry
 * point.
 *
 * <p>The {@code @Environment(EnvType.CLIENT)} annotation is Fabric Loader's - Loom remaps
 * it to the equivalent on Forge/NeoForge - and is why {@code common}'s build script
 * depends on Fabric Loader at all (see the comment there). It stops this class from being
 * loaded on a dedicated server; each loader's client entry point additionally guarantees
 * that by construction (a Fabric {@code ClientModInitializer}, or a NeoForge/Forge
 * {@code @EventBusSubscriber(..., value = Dist.CLIENT)} listener).
 *
 * <p>{@link com.example.examplemod.block.ExampleBlock} needs no client registration (it's a full, opaque cube with the
 * default render type), so there's no real call to make yet - {@code RenderTypeRegistry}'s
 * own render-layer type was itself mid-rewrite across this template's newest supported
 * versions (1.21.2 replaced {@code RenderType} with {@code ChunkSectionLayer} for terrain
 * rendering, and that API kept moving at least as far as 1.21.11), so adding a call here
 * just to have one would mean chasing a moving target for no real benefit - add it (and a
 * Stonecutter split, if the exact call still differs across your supported versions) once
 * you have a block that actually needs one.
 */
@Environment(EnvType.CLIENT)
public final class ExampleModClient {
    private ExampleModClient() {
    }

    /** Called once by every loader's client entry point, after that loader's own client setup. */
    public static void init() {
    }
}
