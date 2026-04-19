package com.hoshihoku.culinarycompat.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.hoshihoku.culinarycompat.CulinaryCompat;

// -1px offset for fd items on the cfb blocks
@Mod.EventBusSubscriber(modid = CulinaryCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModelOffsets {
	static final ModelProperty<Boolean> OFFSET_APPLIES = new ModelProperty<>();

	private static final String CFB_NAMESPACE = "cookingforblockheads";
	private static final Set<ResourceLocation> OFFSET_BLOCKLIST_BELOW = Set
			.of(new ResourceLocation(CFB_NAMESPACE, "cooking_table"));
	private static final float OFFSET_Y = -1.0f / 16.0f;
	private static final int VERTEX_STRIDE_INTS = 8;

	private static final Set<ResourceLocation> TARGETS = Set.of(new ResourceLocation("farmersdelight", "cutting_board"),
			new ResourceLocation("farmersdelight", "skillet"), new ResourceLocation("farmersdelight", "cooking_pot"),
			new ResourceLocation(CulinaryCompat.MODID, "bakeware"));

	private ClientModelOffsets() {
	}

	@SubscribeEvent
	public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
		Map<ResourceLocation, BakedModel> models = event.getModels();
		for (Map.Entry<ResourceLocation, BakedModel> e : models.entrySet()) {
			ResourceLocation key = e.getKey();
			if (key instanceof ModelResourceLocation mrl && "inventory".equals(mrl.getVariant()))
				continue;
			ResourceLocation id = new ResourceLocation(key.getNamespace(), key.getPath());
			if (!TARGETS.contains(id) || e.getValue() instanceof OffsetBakedModel)
				continue;
			e.setValue(new OffsetBakedModel(e.getValue()));
		}
	}

	private static boolean isCfbBelow(BlockAndTintGetter level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(below.getBlock());
		if (id == null || !CFB_NAMESPACE.equals(id.getNamespace()))
			return false;
		return !OFFSET_BLOCKLIST_BELOW.contains(id);
	}

	private static BakedQuad translateY(BakedQuad q, float dy) {
		int[] v = q.getVertices().clone();
		for (int i = 0; i < 4; i++) {
			int yIdx = i * VERTEX_STRIDE_INTS + 1;
			v[yIdx] = Float.floatToRawIntBits(Float.intBitsToFloat(v[yIdx]) + dy);
		}
		return new BakedQuad(v, q.getTintIndex(), q.getDirection(), q.getSprite(), q.isShade());
	}

	static final class OffsetBakedModel implements BakedModel {
		private final BakedModel wrapped;
		private final ConcurrentMap<QuadKey, List<BakedQuad>> cache = new ConcurrentHashMap<>();

		OffsetBakedModel(BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
			ModelData base = wrapped.getModelData(level, pos, state, data);
			if (isCfbBelow(level, pos))
				return base.derive().with(OFFSET_APPLIES, Boolean.TRUE).build();
			return base;
		}

		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
				RenderType rt) {
			List<BakedQuad> base = wrapped.getQuads(state, side, rand, data, rt);
			Boolean flag = data.get(OFFSET_APPLIES);
			if (flag == null || !flag)
				return base;
			return cache.computeIfAbsent(new QuadKey(state, side, rt), k -> {
				if (base.isEmpty())
					return base;
				List<BakedQuad> out = new ArrayList<>(base.size());
				for (BakedQuad q : base)
					out.add(translateY(q, OFFSET_Y));
				return out;
			});
		}

		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
			return wrapped.getQuads(state, side, rand);
		}

		@Override
		public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
			return wrapped.getRenderTypes(state, rand, data);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return wrapped.useAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return wrapped.isGui3d();
		}

		@Override
		public boolean usesBlockLight() {
			return wrapped.usesBlockLight();
		}

		@Override
		public boolean isCustomRenderer() {
			return wrapped.isCustomRenderer();
		}

		@SuppressWarnings("deprecation")
		@Override
		public TextureAtlasSprite getParticleIcon() {
			return wrapped.getParticleIcon();
		}

		@Override
		public TextureAtlasSprite getParticleIcon(ModelData data) {
			return wrapped.getParticleIcon(data);
		}

		@Override
		public ItemTransforms getTransforms() {
			return wrapped.getTransforms();
		}

		@Override
		public ItemOverrides getOverrides() {
			return wrapped.getOverrides();
		}
	}

	private record QuadKey(BlockState state, Direction side, RenderType renderType) {
	}
}
