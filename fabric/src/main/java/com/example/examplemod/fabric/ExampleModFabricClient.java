package com.example.examplemod.fabric;

import com.example.examplemod.client.ExampleModClient;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleModClient.init();
    }
}
