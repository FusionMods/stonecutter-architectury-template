package com.example.examplemod.datagen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Entry point for {@code ./gradlew :datagen:runDatagen} - see README.md's
 * "Data generation" section. Never referenced by the real mod; only this module's own
 * {@code fabric.mod.json} (the {@code "main"} and {@code "fabric-datagen"} entrypoints)
 * know about it.
 *
 * <p>Also implements plain {@link ModInitializer}: {@link DatagenContent}'s throwaway
 * blocks/items use vanilla's direct {@code Registry.register(...)} (the same pattern
 * vanilla itself uses for e.g. {@code Blocks.STONE}), which needs to run during normal mod
 * init, before the registry freezes for the "intrusive holder" mechanism that constructor
 * relies on - touching {@link DatagenContent} here (rather than only lazily, on first use
 * from inside a provider, which runs too late) is what makes that timing work.
 */
public class ExampleModDataGenerator implements DataGeneratorEntrypoint, ModInitializer {
    @Override
    public void onInitialize() {
        // Referencing the class is enough to run its static initializer - see class doc.
        var forceEarlyInit = DatagenContent.EXAMPLE_BLOCK;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ExampleModModelProvider::new);
        pack.addProvider(ExampleModRecipeProvider::new);
        pack.addProvider(ExampleModLootTableProvider::new);
        pack.addProvider(ExampleModLanguageProvider::new);
    }
}
