package com.example.examplemod.registry;

/**
 * Binds every {@code DeferredRegister} above to whichever loader is actually running -
 * call {@link #init()} once, from {@link com.example.examplemod.ExampleMod#init()}.
 * Adding a new content type (a new {@code Mod*} registry class) means adding one line
 * here too.
 */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void init() {
        ModItems.ITEMS.register();
        ModBlocks.BLOCKS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        ModEntities.ENTITIES.register();
        ModSounds.SOUNDS.register();
    }
}
