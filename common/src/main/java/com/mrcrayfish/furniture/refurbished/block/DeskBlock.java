package com.mrcrayfish.furniture.refurbished.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.furniture.refurbished.core.ModTags;
import com.mrcrayfish.furniture.refurbished.data.tag.BlockTagSupplier;
import com.mrcrayfish.furniture.refurbished.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class DeskBlock extends FurnitureHorizontalBlock implements BlockTagSupplier
{
    private static final MapCodec<DeskBlock> CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(WoodType.CODEC.fieldOf("wood_type").forGetter(block -> {
            return block.type;
        }), propertiesCodec()).apply(builder, DeskBlock::new);
    });

    protected final WoodType type;

    public DeskBlock(WoodType type, Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(LEFT, false).setValue(RIGHT, false));
        this.type = type;
    }

    public WoodType getWoodType()
    {
        return this.type;
    }

    @Override
    protected MapCodec<? extends DeskBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected Map<BlockState, VoxelShape> generateShapes(ImmutableList<BlockState> states)
    {
        VoxelShape topShape = Block.box(0, 14, 0, 16, 16, 16);
        VoxelShape backLeftLegShape = Block.box(14, 0, 0, 16, 14, 2);
        VoxelShape backRightLegShape = Block.box(14, 0, 14, 16, 14, 16);
        VoxelShape frontLeftLegShape = Block.box(1, 0, 0, 3, 14, 2);
        VoxelShape frontRightLegShape = Block.box(1, 0, 14, 3, 14, 16);

        ImmutableMap.Builder<BlockState, VoxelShape> builder = new ImmutableMap.Builder<>();
        for(BlockState state : states)
        {
            Direction direction = state.getValue(DIRECTION);
            boolean left = state.getValue(LEFT);
            boolean right = state.getValue(RIGHT);
            List<VoxelShape> shapes = new ArrayList<>();
            shapes.add(topShape);
            if(!left)
            {
                shapes.add(VoxelShapeHelper.rotateHorizontally(backRightLegShape, direction));
                shapes.add(VoxelShapeHelper.rotateHorizontally(frontRightLegShape, direction));
            }
            if(!right)
            {
                shapes.add(VoxelShapeHelper.rotateHorizontally(backLeftLegShape, direction));
                shapes.add(VoxelShapeHelper.rotateHorizontally(frontLeftLegShape, direction));
            }
            builder.put(state, VoxelShapeHelper.combine(shapes));
        }
        return builder.build();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos)
    {
        Direction facing = state.getValue(DIRECTION);
        boolean left = this.isConnectable(level, pos, facing.getClockWise(), facing);
        boolean right = this.isConnectable(level, pos, facing.getCounterClockWise(), facing);
        return state.setValue(LEFT, left).setValue(RIGHT, right);
    }

    public boolean isConnectable(LevelAccessor level, BlockPos pos, Direction checkDirection, Direction tableDirection)
    {
        BlockState state = level.getBlockState(pos.relative(checkDirection));
        return state.getBlock() instanceof DeskBlock && state.getValue(DIRECTION) == tableDirection;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT);
        builder.add(RIGHT);
    }

    @Override
    public List<TagKey<Block>> getTags()
    {
        return List.of(BlockTags.MINEABLE_WITH_AXE, ModTags.Blocks.TUCKABLE);
    }
}
