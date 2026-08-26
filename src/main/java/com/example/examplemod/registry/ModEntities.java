package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

/**
 * Cross-loader entity registry, same {@link DeferredRegister} pattern as
 * {@link ModItems}/{@link ModBlocks}/{@link ModBlockEntities}. No worked example ships
 * with this template - a real custom entity also needs its own attributes and, on the
 * client, a renderer registered from {@link com.example.examplemod.client.ExampleModClient}
 * - but the registry itself is ready to use, e.g.:
 * {@code ModEntities.ENTITIES.register("my_entity", () -> EntityType.Builder.of(MyEntity::new,
 * MobCategory.MISC).build("my_entity"));}
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ExampleMod.MOD_ID, Registries.ENTITY_TYPE);

    private ModEntities() {
    }
}
