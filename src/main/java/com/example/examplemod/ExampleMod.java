package com.example.examplemod;

import com.example.examplemod.network.ModNetworking;
import com.example.examplemod.registry.ModRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point shared by every loader (fabric/forge/neoforge) for this
 * Minecraft version - see the fabric/forge/neoforge projects' own entry
 * point classes for how each loader calls into this class, and README.md
 * for how the multi-version build actually assembles this common project
 * once per targeted Minecraft version.
 *
 * Real content (items, blocks, registries, ...) belongs in the {@code registry}/
 * {@code block} packages next to this class, using Architectury API's cross-loader
 * registry/event/network abstractions (https://docs.architectury.dev/) so it doesn't
 * need per-loader copies - see {@link ModRegistries} and {@link com.example.examplemod.block.ExampleBlock}
 * for the pattern this template ships as a worked example, and README.md's
 * "Adding content" section for an overview. Where an actual Minecraft/Architectury API
 * genuinely changed between 1.20.1, 1.21.1 and 26.2 (as opposed to a loader
 * difference, which Architectury API already handles), reach for a Stonecutter
 * {@code //? if <mc version>} comment instead (e.g. {@code //? if <1.21 {} else {}}) -
 * see https://stonecutter.kikugie.dev/ for the full syntax) - {@link com.example.examplemod.registry.ModSounds}
 * and {@link com.example.examplemod.block.ExampleBlockEntity} both do this for real,
 * narrowly-scoped API changes rather than duplicating whole files.
 */
public final class ExampleMod {
    // Keep this in sync with `modId` in gradle.properties.
    public static final String MOD_ID = "examplemod";

    public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

    private ExampleMod() {
    }

    /** Called once by every loader's entry point, after that loader's own setup. */
    public static void init() {
        ModRegistries.init();
        ModNetworking.init();
        LOGGER.info("Example Mod ({}) initialized", MOD_ID);
    }
}
