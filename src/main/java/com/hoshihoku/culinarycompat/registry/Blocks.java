package com.hoshihoku.culinarycompat.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class Blocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			CulinaryCompat.MODID);

	public static final RegistryObject<Block> BAKEWARE = BLOCKS.register("bakeware",
			() -> new Bakeware(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f)
					.sound(SoundType.METAL).noOcclusion()));

	private Blocks() {
	}

	public static class Bakeware extends HorizontalDirectionalBlock {
		private static final String CFB_NAMESPACE = "cookingforblockheads";
		private static final double OFFSET = -1.0 / 16.0;
		private static final VoxelShape SHAPE_NS = Shapes.or(Block.box(2, 0, 3, 14, 4, 13),
				Block.box(1, 2, 6, 2, 4, 10), Block.box(14, 2, 6, 15, 4, 10));
		private static final VoxelShape SHAPE_EW = Shapes.or(Block.box(3, 0, 2, 13, 4, 14),
				Block.box(6, 2, 1, 10, 4, 2), Block.box(6, 2, 14, 10, 4, 15));
		private static final VoxelShape SHAPE_NS_OFFSET = SHAPE_NS.move(0, OFFSET, 0);
		private static final VoxelShape SHAPE_EW_OFFSET = SHAPE_EW.move(0, OFFSET, 0);

		public Bakeware(Properties props) {
			super(props);
			this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
		}

		@Override
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
			builder.add(FACING);
		}

		@Override
		public BlockState getStateForPlacement(BlockPlaceContext context) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}

		@Override
		public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
			Direction facing = state.getValue(FACING);
			boolean ns = facing.getAxis() == Direction.Axis.Z;
			BlockState below = level.getBlockState(pos.below());
			ResourceLocation belowId = ForgeRegistries.BLOCKS.getKey(below.getBlock());
			boolean onCfb = belowId != null && CFB_NAMESPACE.equals(belowId.getNamespace());
			if (onCfb)
				return ns ? SHAPE_NS_OFFSET : SHAPE_EW_OFFSET;
			return ns ? SHAPE_NS : SHAPE_EW;
		}
	}
}
