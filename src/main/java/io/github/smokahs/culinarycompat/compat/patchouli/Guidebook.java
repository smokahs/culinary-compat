package io.github.smokahs.culinarycompat.compat.patchouli;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import io.github.smokahs.culinarycompat.CulinaryCompat;

public class Guidebook extends Item {

	private static final String PATCHOULI_MODID = "patchouli";
	private static final ResourceLocation BOOK_ID = new ResourceLocation(CulinaryCompat.MODID, "guide");

	public Guidebook(Properties props) {
		super(props.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
			return InteractionResultHolder.success(stack);
		}
		if (!ModList.get().isLoaded(PATCHOULI_MODID)) {
			sp.sendSystemMessage(Component.literal("Patchouli is not installed.").withStyle(ChatFormatting.RED));
			return InteractionResultHolder.fail(stack);
		}
		try {
			Class<?> apiCls = Class.forName("vazkii.patchouli.api.PatchouliAPI");
			Object api = apiCls.getMethod("get").invoke(null);
			api.getClass().getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class).invoke(api, sp,
					BOOK_ID);
		} catch (Exception e) {
			CulinaryCompat.LOGGER.error("Failed to open Head Chef's Journal", e);
			return InteractionResultHolder.fail(stack);
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.culinarycompat.guidebook.subtitle").withStyle(ChatFormatting.GRAY));
	}
}
