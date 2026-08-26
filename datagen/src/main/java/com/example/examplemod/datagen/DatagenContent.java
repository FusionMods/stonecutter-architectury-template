package com.example.examplemod.datagen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Datagen runs standalone, pinned to one fixed Minecraft version, with no dependency on
 * the real per-version {@code common} project (see README.md's "Data generation" section
 * for why) - so it needs its own throwaway {@link Block}/{@link Item} instances, registered
 * under the exact same ids as the real mod's {@code registry/ModBlocks.java}/
 * {@code registry/ModItems.java}, purely so the provider APIs below have something to
 * point at.
 *
 * <p>These are <b>not</b> the real mod's registered content, just enough to generate
 * correct JSON for the same ids - add a matching entry here whenever you add real content
 * that needs data generation.
 */
public final class DatagenContent {
    public static final String MOD_ID = "examplemod";

    public static final Block EXAMPLE_BLOCK =
            register("example_block", new Block(BlockBehaviour.Properties.of()));
    public static final Item EXAMPLE_BLOCK_ITEM =
            registerItem("example_block", new BlockItem(EXAMPLE_BLOCK, new Item.Properties()));
    public static final Item EXAMPLE_ITEM =
            registerItem("example_item", new Item(new Item.Properties()));

    private DatagenContent() {
    }

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, name), block);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, name), item);
    }
}
