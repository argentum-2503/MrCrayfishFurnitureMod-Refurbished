package com.mrcrayfish.furniture.refurbished.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.furniture.refurbished.blockentity.WorkbenchBlockEntity;
import com.mrcrayfish.furniture.refurbished.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class WorkbenchBlock extends FurnitureHorizontalBlock implements EntityBlock
{
    public WorkbenchBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected Map<BlockState, VoxelShape> generateShapes(ImmutableList<BlockState> states)
    {
        VoxelShape baseShape = Block.box(0, 0, 0, 16, 13, 16);
        return ImmutableMap.copyOf(states.stream().collect(Collectors.toMap(state -> state, o -> baseShape)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench)
        {
            if(!workbench.isOccupied())
            {
                if(player.openMenu(workbench).isPresent())
                {
                    workbench.sendCountsToUser(true);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new WorkbenchBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTicker(level, type, ModBlockEntities.WORKBENCH.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> type, BlockEntityType<? extends WorkbenchBlockEntity> workbench)
    {
        if(!level.isClientSide())
        {
            return createTickerHelper(type, workbench, WorkbenchBlockEntity::sendCountsToUser);
        }
        return null;
    }
}
