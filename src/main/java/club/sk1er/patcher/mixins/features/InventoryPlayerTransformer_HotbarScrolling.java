package club.sk1er.patcher.mixins.features;

import club.sk1er.patcher.config.PatcherConfig;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerTransformer_HotbarScrolling {
    @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
    private void patcher$cancelScrolling(int direction, CallbackInfo ci) {
        if (PatcherConfig.disableHotbarScrolling) {
            ci.cancel();
        }
    }
}
