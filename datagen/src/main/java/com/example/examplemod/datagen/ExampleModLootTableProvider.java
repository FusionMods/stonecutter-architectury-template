package com.example.examplemod.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

/** {@code example_block} drops itself when mined - without this it would drop nothing. */
public class ExampleModLootTableProvider extends FabricBlockLootTableProvider {
    protected ExampleModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(DatagenContent.EXAMPLE_BLOCK);
    }
}
