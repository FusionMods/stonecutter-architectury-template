package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Cross-loader item registry, backed by Architectury API's {@link DeferredRegister}
 * (see https://docs.architectury.dev/api/registry) - one {@code register(...)} call
 * here reaches Fabric, Forge and NeoForge alike, with no per-loader copy needed.
 * {@link ModRegistries#init()} calls {@link #ITEMS}{@code .register()} once, to actually
 * bind this to whichever loader ends up running.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ExampleMod.MOD_ID, Registries.ITEM);

    /** Worked example - a plain item, registered so it actually exists to go with the model/recipe {@code datagen/} generates for it. */
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = registerSimple("example_item", new Item.Properties());

    private ModItems() {
    }

    /** Registers a plain {@link Item}. */
    public static RegistrySupplier<Item> registerSimple(String name, Item.Properties properties) {
        return ITEMS.register(name, () -> new Item(properties));
    }

    /** Registers a {@link BlockItem} for a block registered elsewhere - see {@link ModBlocks#registerWithItem}. */
    public static RegistrySupplier<Item> registerBlockItem(String name, Supplier<? extends Block> block, Item.Properties properties) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), properties));
    }
}
