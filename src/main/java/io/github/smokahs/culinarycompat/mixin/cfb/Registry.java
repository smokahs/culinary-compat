package com.hoshihoku.culinarycompat.mixin.cfb;

import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;

import com.hoshihoku.culinarycompat.compat.cfb.FarmersDelight;
import com.hoshihoku.culinarycompat.compat.cfb.Pam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CookingRegistry.class, remap = false)
public abstract class Registry {
	@Inject(method = "initFoodRegistry(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/core/RegistryAccess;)V", at = @At("RETURN"), remap = false, require = 0)
	private static void culinarycompat$afterInit(RecipeManager recipeManager, RegistryAccess registryAccess,
			CallbackInfo ci) {
		FarmersDelight.Cutting.registerBridges(recipeManager, registryAccess);
		FarmersDelight.Campfire.registerBridges(recipeManager, registryAccess);
		if (ModList.get().isLoaded("farmersdelight")) {
			FarmersDelight.Pot.registerBridges(recipeManager, registryAccess);
		}
		if (ModList.get().isLoaded(Pam.PAM_MODID)) {
			Pam.Cutting.registerBridges(recipeManager, registryAccess);
			Pam.Skillet.registerBridges(recipeManager, registryAccess);
			Pam.Pot.registerBridges(recipeManager, registryAccess);
			Pam.Bakeware.registerBridges(recipeManager, registryAccess);
		}
	}
}
