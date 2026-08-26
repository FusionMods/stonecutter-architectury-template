package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ExampleBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

/**
 * Cross-loader block registry, same {@link DeferredRegister} pattern as {@link ModItems}.
 * {@link #registerWithItem} additionally registers the matching
 * {@link net.minecraft.world.item.BlockItem} through {@link ModItems}, since almost every
 * block needs one - reach for {@link #registerBlockOnly} for the rare block that
 * deliberately doesn't (fire, water, ...).
 */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ExampleMod.MOD_ID, Registries.BLOCK);

    /** Worked example - see {@link ExampleBlock} and its paired {@link com.example.examplemod.block.ExampleBlockEntity}. */
    public static final RegistrySupplier<ExampleBlock> EXAMPLE_BLOCK = registerWithItem(
            "example_block",
            () -> new ExampleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f)),
            new Item.Properties());

    private ModBlocks() {
    }

    /** Registers a block with no accompanying item - see {@link #registerWithItem} for the common case. */
    public static <T extends Block> RegistrySupplier<T> registerBlockOnly(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    /** Registers a block and a matching {@link net.minecraft.world.item.BlockItem} for it. */
    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block, Item.Properties itemProperties) {
        RegistrySupplier<T> registered = BLOCKS.register(name, block);
        ModItems.registerBlockItem(name, registered, itemProperties);
        return registered;
    }
}
