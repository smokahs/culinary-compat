package com.hoshihoku.culinarycompat.mixin.cfb;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;

import com.hoshihoku.culinarycompat.compat.cfb.CfbCampfireBridge;
import com.hoshihoku.culinarycompat.compat.cfb.CfbCuttingBridge;
import com.hoshihoku.culinarycompat.compat.cfb.FdPotBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamBakewareBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamCuttingBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamPotBridge;
import com.hoshihoku.culinarycompat.compat.cfb.PamSkilletBridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CookingRegistry.class, remap = false)
public abstract class CookingRegistryMixin {
	@Inject(method = "initFoodRegistry(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/core/RegistryAccess;)V", at = @At("RETURN"), remap = false, require = 0)
	private static void culinarycompat$afterInit(RecipeManager recipeManager, RegistryAccess registryAccess,
			CallbackInfo ci) {
		CfbCuttingBridge.registerBridges(recipeManager, registryAccess);
		CfbCampfireBridge.registerBridges(recipeManager, registryAccess);
		if (ModList.get().isLoaded("farmersdelight")) {
			FdPotBridge.registerBridges(recipeManager, registryAccess);
		}
		if (ModList.get().isLoaded(PamCuttingBridge.PAM_MODID)) {
			PamCuttingBridge.registerBridges(recipeManager, registryAccess);
			PamSkilletBridge.registerBridges(recipeManager, registryAccess);
			PamPotBridge.registerBridges(recipeManager, registryAccess);
			PamBakewareBridge.registerBridges(recipeManager, registryAccess);
		}
	}
}
