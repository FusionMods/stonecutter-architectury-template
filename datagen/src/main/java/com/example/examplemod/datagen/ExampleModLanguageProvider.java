package com.example.examplemod.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

/**
 * Generates {@code en_us.json} for {@link DatagenContent}'s block/item, plus the creative
 * tab title - see {@code registry/ModCreativeTabs.java} in the real mod. Without this,
 * those all show their raw, untranslated registry key in-game (e.g.
 * {@code "block.examplemod.example_block"}) instead of a readable name.
 */
public class ExampleModLanguageProvider extends FabricLanguageProvider {
    public ExampleModLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(DatagenContent.EXAMPLE_BLOCK, "Example Block");
        translationBuilder.add(DatagenContent.EXAMPLE_ITEM, "Example Item");
        translationBuilder.add("itemGroup." + DatagenContent.MOD_ID, "Example Mod");
    }
}
