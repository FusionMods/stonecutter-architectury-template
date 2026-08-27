package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Cross-loader creative tab. Same {@link DeferredRegister} pattern as
 * {@link ModItems}/{@link ModBlocks} - not the shortcut it first looks like: Architectury
 * API's {@link CreativeTabRegistry#create}/{@code #appendBuiltin} pairing (a natural first
 * reach, since {@code create} looks like it should register the tab itself) turned out to be
 * for adding entries to an *existing, already-registered* tab - either a vanilla one or one
 * of yours registered some other way - not for registering a new one; calling
 * {@code appendBuiltin} on a tab {@code create} just built throws
 * {@code IllegalArgumentException: Builtin tab ... is not registered!} the moment any code
 * path actually runs (only caught by the {@code fabric/src/gametest/} GameTests actually
 * booting a server - see README.md's "Testing" section - {@code chiseledBuild} alone doesn't
 * exercise this at all, since it only compiles). Wrapping the tab in an ordinary
 * {@code DeferredRegister<CreativeModeTab>} sidesteps the question entirely: it handles
 * per-loader registration timing the same proven way it does for every other content type
 * here, and {@link CreativeTabRegistry#append(dev.architectury.registry.registries.DeferredSupplier, net.minecraft.world.level.ItemLike...)}
 * accepts a {@code RegistrySupplier} directly ({@code RegistrySupplier} extends Architectury's
 * {@code DeferredSupplier}).
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(ExampleMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /** Worked example - holds {@link ModItems#EXAMPLE_ITEM} and {@link ModBlocks#EXAMPLE_BLOCK}. */
    public static final RegistrySupplier<CreativeModeTab> EXAMPLE_TAB = TABS.register("example_tab", () ->
            CreativeTabRegistry.create(
                    Component.translatable("itemGroup." + ExampleMod.MOD_ID),
                    () -> new ItemStack(ModItems.EXAMPLE_ITEM.get())));

    private ModCreativeTabs() {
    }

    /** Called from {@link ModRegistries#init()}, after every other {@code Mod*} registry has bulk-registered. */
    public static void init() {
        TABS.register();
        CreativeTabRegistry.append(EXAMPLE_TAB, ModItems.EXAMPLE_ITEM);
        CreativeTabRegistry.append(EXAMPLE_TAB, ModBlocks.EXAMPLE_BLOCK);
    }
}
