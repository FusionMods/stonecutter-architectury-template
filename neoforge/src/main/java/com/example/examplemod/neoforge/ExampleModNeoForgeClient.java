package com.example.examplemod.neoforge;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.ExampleModClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// value = Dist.CLIENT tells NeoForge to skip loading this class entirely on a dedicated
// server, so ExampleModClient (and whatever client-only classes it in turn references)
// never needs to exist there. Which annotation to use is the one genuine difference this
// hits across NeoForge versions: 1.20.5 moved @EventBusSubscriber out from under Mod and
// dropped the explicit `bus` (it's always the mod bus now); before that it's still
// Forge's original nested, bus-explicit @Mod.EventBusSubscriber.
//? if >=1.20.5 {
@net.neoforged.fml.common.EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
//?} else {
/*@net.neoforged.fml.common.Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = net.neoforged.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)*/
//?}
public final class ExampleModNeoForgeClient {
    private ExampleModNeoForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ExampleModClient.init();
    }
}
