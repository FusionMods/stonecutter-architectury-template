package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Paired with {@link ExampleBlock}. Stores one plain {@code int} in NBT to demonstrate
 * block entity data persistence - this hit three genuine Minecraft *version* differences,
 * all handled with Stonecutter {@code //? if} blocks below rather than any loader-specific
 * code, since none of them have anything to do with which loader is running (see
 * {@link ExampleMod} for that split's general rationale): 1.20.5 added a
 * {@code HolderLookup.Provider} parameter to both save and load; 1.21.5 changed
 * {@code CompoundTag#getInt} to return {@code Optional<Integer>} instead of a plain
 * {@code int}; and 1.21.6 replaced the whole {@code CompoundTag}-based save/load with the
 * {@code ValueInput}/{@code ValueOutput} abstraction (https://docs.neoforged.net/docs/blockentities/).
 */
public class ExampleBlockEntity extends BlockEntity {
    private static final String COUNTER_KEY = "Counter";

    private int counter;

    public ExampleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXAMPLE_BLOCK_ENTITY.get(), pos, state);
    }

    /** Called from {@link ExampleBlock}'s interaction override. */
    public void incrementCounter() {
        counter++;
        ExampleMod.LOGGER.info("{} clicked {} time(s)", ExampleMod.MOD_ID, counter);
        setChanged();
    }

    /** Read by {@link ExampleBlock}'s interaction override to sync the new value to the client - see {@link com.example.examplemod.network.ModNetworking}. */
    public int getCounter() {
        return counter;
    }

    // Flattened into four independent branches (rather than nesting a second //? split
    // inside one of these) since 1.20.5-1.21.4 and 1.21.5 need the exact same
    // saveAdditional/loadAdditional signature but a different CompoundTag#getInt call -
    // see the class doc above for what changed where.
    //? if >=1.21.6 {
    /*
    @Override
    public void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(COUNTER_KEY, counter);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        counter = input.getIntOr(COUNTER_KEY, 0);
    }
    */
    //?} else if >=1.21.5 {
    /*
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(COUNTER_KEY, counter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        counter = tag.getInt(COUNTER_KEY).orElse(0);
    }
    */
    //?} else if >=1.20.5 {
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(COUNTER_KEY, counter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        counter = tag.getInt(COUNTER_KEY);
    }
    //?} else {
    /*
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COUNTER_KEY, counter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        counter = tag.getInt(COUNTER_KEY);
    }
    */
    //?}
}
