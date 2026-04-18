package com.hoshihoku.culinarycompat.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.hoshihoku.culinarycompat.CulinaryCompat;

public final class ModSounds {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
			CulinaryCompat.MODID);

	public static final RegistryObject<SoundEvent> DING = SOUNDS.register("ding",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CulinaryCompat.MODID, "ding")));

	private ModSounds() {
	}
}
