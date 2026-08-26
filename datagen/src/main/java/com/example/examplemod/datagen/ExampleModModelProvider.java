package com.example.examplemod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

/**
 * Generates {@code example_block}'s block model + blockstate, and {@code example_item}'s
 * item model. {@code example_block}'s item model comes free with
 * {@link BlockModelGenerators#createTrivialCube} - vanilla's own convention for a simple
 * cube block is to reuse the block model as the item model.
 *
 * <p>Deliberately a single blockstate variant, ignoring {@link com.example.examplemod.block.ExampleBlock#FACING}
 * - that property exists for the interaction/data-storage worked example in {@code common},
 * not to justify directional-model generation here. A real directional block would want
 * {@code createOrientableTrivial}/a {@code PropertyDispatch} keyed on {@code FACING} instead.
 */
public class ExampleModModelProvider extends FabricModelProvider {
    public ExampleModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createTrivialCube(DatagenContent.EXAMPLE_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(DatagenContent.EXAMPLE_ITEM, ModelTemplates.FLAT_ITEM);
    }

    @Override
    public String getName() {
        return "Example Mod block/item models";
    }
}
