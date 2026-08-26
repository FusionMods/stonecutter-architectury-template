package com.example.examplemod.forge;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.ExampleModClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// value = Dist.CLIENT tells Forge to skip loading this class entirely on a dedicated
// server, so ExampleModClient (and whatever client-only classes it in turn references)
// never needs to exist there.
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ExampleModForgeClient {
    private ExampleModForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ExampleModClient.init();
    }
}
