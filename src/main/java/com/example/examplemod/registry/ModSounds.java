package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

/**
 * Cross-loader sound registry, same {@link DeferredRegister} pattern as
 * {@link ModItems}/{@link ModBlocks}. {@link #id} is the one spot this hits a genuine
 * Minecraft *version* difference, and it's actually two: 1.21 made {@code ResourceLocation}'s
 * constructor private in favour of a {@code fromNamespaceAndPath} factory, and 1.21.11
 * renamed the whole class to {@code Identifier} (confirmed by bisecting a real compile
 * failure - not 26.1 as the class name might suggest; the rename landed one version before
 * the year.drop rebrand, not with it) - neither is a loader difference, so both are
 * Stonecutter {@code //? if} blocks, not another abstraction. The return type itself changes
 * between branches, so the whole method declaration (not just its body) is duplicated per
 * branch below.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ExampleMod.MOD_ID, Registries.SOUND_EVENT);

    private ModSounds() {
    }

    /** Registers a variable-range {@link SoundEvent} - the usual choice for a mod's own sounds. */
    public static RegistrySupplier<SoundEvent> registerVariable(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
    }

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
