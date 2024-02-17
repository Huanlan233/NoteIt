package top.htext.noteit.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
	@Inject(method = "getAdjustedPitch", at = @At("RETURN"), cancellable = true)
	private void preventPitchClamp( SoundInstance sound, CallbackInfoReturnable<Float> cir ) {
		cir.setReturnValue(MathHelper.clamp(sound.getPitch(), 0.0F, 10.0F));
		// 返回值原本为 MathHelper.clamp(sound.getPitch(), 0.5F, 2.0F); 被 MathHelper 截断了。
	}
}
