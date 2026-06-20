package io.github.smokahs.culinarycompat.mixin;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public class CulinaryMixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = LogManager.getLogger("culinarycompat-mixin");
	private static final String FD_CUTTING_BOARD_ENTITY = "vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity";

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith(".fd.CuttingBoardCap")) {
			return fdAddItemReturnsItemStack();
		}
		return true;
	}

	private static boolean fdAddItemReturnsItemStack() {
		try {
			ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(FD_CUTTING_BOARD_ENTITY);
			for (MethodNode m : node.methods) {
				if (m.name.equals("addItem") && m.desc.endsWith(")Lnet/minecraft/world/item/ItemStack;")) {
					LOGGER.info("FD addItem returns ItemStack; applying cutting board cap mixin");
					return true;
				}
			}
			LOGGER.info("FD addItem does not return ItemStack; skipping cutting board cap mixin");
		} catch (Throwable t) {
			LOGGER.warn("Could not inspect FD addItem signature; skipping cutting board cap mixin", t);
		}
		return false;
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
