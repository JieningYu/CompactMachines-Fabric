package dev.compactmods.machines.block.walls;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ProtectedWallBlock extends Block {
    protected ProtectedWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        // NO-Op, do not destroy on explosions
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return false;
    }
}