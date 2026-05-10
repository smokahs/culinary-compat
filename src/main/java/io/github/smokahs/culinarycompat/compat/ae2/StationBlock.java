package io.github.smokahs.culinarycompat.compat.ae2;

import java.util.List;

import javax.annotation.Nullable;

import net.blay09.mods.cookingforblockheads.block.BlockKitchen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class StationBlock extends BlockKitchen {

	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

	static final double OFFSET_Y = 1.0 / 16.0;

	private static final VoxelShape SHAPE_NORTH = Block.box(3, -1, 6, 13, 7.5, 13);
	private static final VoxelShape SHAPE_SOUTH = Block.box(3, -1, 3, 13, 7.5, 10);
	private static final VoxelShape SHAPE_EAST = Block.box(3, -1, 3, 10, 7.5, 13);
	private static final VoxelShape SHAPE_WEST = Block.box(6, -1, 3, 13, 7.5, 13);
	private static final VoxelShape SHAPE_NORTH_OFFSET = SHAPE_NORTH.move(0, OFFSET_Y, 0);
	private static final VoxelShape SHAPE_SOUTH_OFFSET = SHAPE_SOUTH.move(0, OFFSET_Y, 0);
	private static final VoxelShape SHAPE_EAST_OFFSET = SHAPE_EAST.move(0, OFFSET_Y, 0);
	private static final VoxelShape SHAPE_WEST_OFFSET = SHAPE_WEST.move(0, OFFSET_Y, 0);

	// shift up 1px when the block below is full block, OR is
	// cfb:cooking_table
	static boolean shouldOffsetOn(BlockGetter level, BlockPos stationPos) {
		BlockPos below = stationPos.below();
		BlockState belowState = level.getBlockState(below);
		if (belowState.isFaceSturdy(level, below, Direction.UP)) {
			return true;
		}
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(belowState.getBlock());
		return id != null && "cookingforblockheads".equals(id.getNamespace()) && "cooking_table".equals(id.getPath());
	}

	public StationBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.5f),
				Bridge.KITCHEN_STATION.getId());
		registerDefaultState(getStateDefinition().any().setValue(CONNECTED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(CONNECTED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		boolean offset = shouldOffsetOn(world, pos);
		switch (state.getValue(FACING)) {
			case NORTH :
				return offset ? SHAPE_NORTH_OFFSET : SHAPE_NORTH;
			case WEST :
				return offset ? SHAPE_WEST_OFFSET : SHAPE_WEST;
			case EAST :
				return offset ? SHAPE_EAST_OFFSET : SHAPE_EAST;
			case SOUTH :
			default :
				return offset ? SHAPE_SOUTH_OFFSET : SHAPE_SOUTH;
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StationBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof StationBlockEntity be) {
			be.applyDataFromItemToBlockEntity(stack);
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide
				? null
				: createTickerHelper(type, Bridge.KITCHEN_STATION_BE.get(), StationBlockEntity::serverTick);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (player instanceof ServerPlayer sp && level.getBlockEntity(pos) instanceof StationBlockEntity station) {
			NetworkHooks.openScreen(sp, station, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		// green "Linked" tooltip like terminals, worth it
		var tag = stack.getTag();
		if (tag != null && tag.contains(StationItem.TAG_ACCESS_POINT_POS)) {
			tooltip.add(Component.translatable("tooltip.culinarycompat:ae2_kitchen_station.linked")
					.withStyle(ChatFormatting.GREEN));
		}
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("tooltip.culinarycompat:ae2_kitchen_station.shift")
					.withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable("tooltip.culinarycompat:ae2_kitchen_station.hold_shift")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
