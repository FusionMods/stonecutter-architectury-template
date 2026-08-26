package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Worked example tying the registration/interaction/data-storage pattern together: a
 * block that remembers which way it's facing - {@link #FACING} is a plain vanilla
 * blockstate property, no loader or Minecraft-version differences here at all - and
 * stores one extra value (a click counter) on its paired {@link ExampleBlockEntity}.
 * Registered through {@link com.example.examplemod.registry.ModBlocks}.
 */
public class ExampleBlock extends Block implements EntityBlock {
    // EnumProperty<Direction>, not the old DirectionProperty (removed in 1.21.2) - the
    // wider type is assignment-compatible with BlockStateProperties.FACING either way,
    // so no Stonecutter split is needed for this one.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public ExampleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // public, not protected: BlockBehaviour declares these public in 1.20.1 and protected
    // from 1.20.5 on - public is always a valid (widening) override of either.
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExampleBlockEntity(pos, state);
    }

    // The interaction method itself is the one genuine Minecraft *version* difference
    // this block hits: 1.20.5 split the old single `use` override into `useItemOn` (item
    // in hand) and `useWithoutItem` (empty hand) - see https://docs.neoforged.net/docs/1.21.2/items/interactionpipeline/
    // for the full pipeline. Nothing loader-specific here, so this is a Stonecutter
    // //? if block, not another abstraction.
    //? if >=1.20.5 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        return interact(level, pos);
    }
    //?} else {
    /*
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        return interact(level, pos);
    }
    */
    //?}

    private InteractionResult interact(Level level, BlockPos pos) {
        if (!isClientSide(level) && level.getBlockEntity(pos) instanceof ExampleBlockEntity blockEntity) {
            blockEntity.incrementCounter();
        }
        return InteractionResult.SUCCESS;
    }

    // Level#isClientSide went from a public field to a private one (with this accessor
    // method instead) in 1.21.2 - another narrowly-scoped version difference.
    //? if >=1.21.2 {
    /*
    private static boolean isClientSide(Level level) {
        return level.isClientSide();
    }
    */
    //?} else {
    private static boolean isClientSide(Level level) {
        return level.isClientSide;
    }
    //?}
}
