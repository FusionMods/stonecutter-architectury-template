package com.example.examplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point shared by every loader (fabric/forge/neoforge) for this
 * Minecraft version - see the fabric/forge/neoforge projects' own entry
 * point classes for how each loader calls into this class, and README.md
 * for how the multi-version build actually assembles this common project
 * once per targeted Minecraft version.
 *
 * This is deliberately bare: real content (items, blocks, registries)
 * belongs here too once you add it, using Architectury API's cross-loader
 * registry/event/network abstractions (https://docs.architectury.dev/) so
 * it doesn't need per-loader copies. Where an actual Minecraft/Architectury
 * API genuinely changed between 1.20.1, 1.21.1 and 26.2, reach for a
 * Stonecutter {@code //? if <mc version>} comment instead (e.g.
 * {@code //? if <1.21 {} else {}} - see https://stonecutter.kikugie.dev/
 * for the full syntax) - but only where you've actually hit such a
 * difference; this class doesn't need one yet.
 */
public final class ExampleMod {
    // Keep this in sync with `modId` in gradle.properties.
    public static final String MOD_ID = "examplemod";

    public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

    private ExampleMod() {
    }

    /** Called once by every loader's entry point, after that loader's own setup. */
    public static void init() {
        LOGGER.info("Example Mod ({}) initialized", MOD_ID);
    }
}
