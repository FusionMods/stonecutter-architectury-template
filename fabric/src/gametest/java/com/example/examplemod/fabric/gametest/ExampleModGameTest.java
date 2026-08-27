package com.example.examplemod.fabric.gametest;

import com.example.examplemod.block.ExampleBlock;
import com.example.examplemod.block.ExampleBlockEntity;
import com.example.examplemod.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTests for the {@code ExampleBlock}/{@code ExampleBlockEntity} worked example - see
 * README.md's "Testing" section for why this exists only for whichever Minecraft version is
 * currently {@code stonecutter active} (see {@code fabric/build.gradle.kts}), not the whole
 * matrix. Run with {@code ./gradlew :fabric:<activeVersion>:runGameTest}, or automatically as
 * part of a normal build/CI run.
 */
public class ExampleModGameTest implements FabricGameTest {
    /** Placing the block records which way the player was facing at the time. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void exampleBlockTracksFacing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.EXAMPLE_BLOCK.get().defaultBlockState().setValue(ExampleBlock.FACING, Direction.EAST));

        helper.assertBlockProperty(pos, ExampleBlock.FACING, Direction.EAST);
        helper.succeed();
    }

    /** Each interaction increments the block entity's click counter by one - see {@link ExampleBlock}. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void exampleBlockEntityCounterIncrements(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.EXAMPLE_BLOCK.get());

        if (!(helper.getBlockEntity(pos) instanceof ExampleBlockEntity blockEntity)) {
            helper.fail("Expected an ExampleBlockEntity at " + pos);
            return;
        }

        blockEntity.incrementCounter();
        blockEntity.incrementCounter();
        helper.assertTrue(blockEntity.getCounter() == 2, "Expected counter to be 2 after two increments, was " + blockEntity.getCounter());
        helper.succeed();
    }
}
