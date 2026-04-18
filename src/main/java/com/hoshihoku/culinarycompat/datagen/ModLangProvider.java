package com.hoshihoku.culinarycompat.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class ModLangProvider extends LanguageProvider {
	public ModLangProvider(PackOutput output) {
		super(output, CulinaryCompat.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
	}
}
