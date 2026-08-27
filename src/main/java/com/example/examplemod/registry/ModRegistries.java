package com.example.examplemod.registry;

/**
 * Binds every {@code DeferredRegister} above to whichever loader is actually running -
 * call {@link #init()} once, from {@link com.example.examplemod.ExampleMod#init()}.
 * Adding a new content type (a new {@code Mod*} registry class) means adding one line
 * here too. {@link ModCreativeTabs} runs last, since it references content the other
 * registries just bound.
 *
 * <p><b>{@code ModBlocks} must bulk-register before {@code ModItems}</b> - caught by the
 * {@code fabric/src/gametest/} GameTests (see README.md's "Testing" section) actually
 * booting a server, not by {@code chiseledBuild}, which only proves things compile. On
 * Fabric, a {@code DeferredRegister} entry added *after* its container's own bulk
 * {@code register()} call already ran gets registered eagerly, on the spot, instead of
 * waiting - and {@link ModBlocks#registerWithItem} adds exactly such a late entry to
 * {@link ModItems#ITEMS} (the matching {@code BlockItem}) as a side effect of registering a
 * block. If {@code ModItems.ITEMS.register()} had already run by then, that eager
 * registration resolves the {@code BlockItem}'s supplier immediately - which calls
 * {@code .get()} on the block's own {@code RegistrySupplier} - before the block itself has
 * actually been bulk-registered, throwing {@code NullPointerException: Registry Object not
 * present}. Blocks first avoids that ordering trap; keep it that way if you reorder this
 * method, and put anything else that reaches into another registry as a side effect (like
 * {@code registerWithItem} does) before that other registry's own bulk {@code register()}
 * call.
 */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void init() {
        ModBlocks.BLOCKS.register();
        ModItems.ITEMS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        ModEntities.ENTITIES.register();
        ModSounds.SOUNDS.register();
        ModCreativeTabs.init();
    }
}
