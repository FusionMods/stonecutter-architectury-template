package com.example.examplemod.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

/**
 * One shaped and one shapeless recipe, covering both common recipe shapes: a vanilla
 * material into {@code example_item} (shaped), then {@code example_item} into
 * {@code example_block} (shapeless) - purely illustrative, not meant to be balanced.
 *
 * <p>Recipes save under this datagen module's own mod id by default (not the output
 * item's) - see this module's {@code fabric.mod.json}, which deliberately shares
 * {@link DatagenContent#MOD_ID} for exactly this reason.
 */
public class ExampleModRecipeProvider extends FabricRecipeProvider {
    public ExampleModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DatagenContent.EXAMPLE_ITEM)
                .pattern("###")
                .pattern("#Q#")
                .pattern("###")
                .define('#', Items.IRON_INGOT)
                .define('Q', Items.QUARTZ)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, DatagenContent.EXAMPLE_BLOCK)
                .requires(DatagenContent.EXAMPLE_ITEM, 4)
                .unlockedBy("has_example_item", has(DatagenContent.EXAMPLE_ITEM))
                .save(output);
    }

    @Override
    public String getName() {
        return "Example Mod recipes";
    }
}
