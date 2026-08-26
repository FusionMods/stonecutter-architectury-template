package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ExampleBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Cross-loader block entity registry, same {@link DeferredRegister} pattern as
 * {@link ModItems}/{@link ModBlocks}. {@link #newType} is the one genuine Minecraft
 * *version* difference this hits, and it's an unusually sharp one: 1.21.2 removed
 * {@code BlockEntityType.Builder} *and* privatized {@code BlockEntityType}'s constructor,
 * with no vanilla public replacement. Fabric API and NeoForge each patch in their own
 * widened path for their loader specifically (Fabric API's {@code FabricBlockEntityTypeBuilder},
 * NeoForge's own access-transformed constructor) - see
 * https://docs.fabricmc.net/develop/blocks/block-entities and
 * https://docs.neoforged.net/docs/blockentities/ - but neither is available from shared
 * {@code common} code, so there's no single vanilla or Architectury API call to make here.
 * {@link #newType} reaches through reflection instead for versions that need it, to keep
 * this one call shared rather than duplicating block entity registration per loader.
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ExampleMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    /** Worked example - see {@link ExampleBlockEntity} and its paired {@link com.example.examplemod.block.ExampleBlock}. */
    public static final RegistrySupplier<BlockEntityType<?>> EXAMPLE_BLOCK_ENTITY =
            register("example_block_entity", ExampleBlockEntity::new, ModBlocks.EXAMPLE_BLOCK);

    private ModBlockEntities() {
    }

    public static RegistrySupplier<BlockEntityType<?>> register(
            String name, BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory, Supplier<? extends Block> block) {
        return BLOCK_ENTITIES.register(name, () -> newType(factory, block.get()));
    }

    //? if >=1.21.2 {
    /*
    private static BlockEntityType<?> newType(BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory, Block block) {
        try {
            var constructor = BlockEntityType.class.getDeclaredConstructor(BlockEntityType.BlockEntitySupplier.class, java.util.Set.class);
            constructor.setAccessible(true);
            return (BlockEntityType<?>) constructor.newInstance(factory, java.util.Set.of(block));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct BlockEntityType for " + block, e);
        }
    }
    */
    //?} else {
    private static BlockEntityType<?> newType(BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory, Block block) {
        return BlockEntityType.Builder.of(factory, block).build(null);
    }
    //?}
}
