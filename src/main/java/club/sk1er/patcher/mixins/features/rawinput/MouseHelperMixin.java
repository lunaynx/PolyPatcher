package club.sk1er.patcher.mixins.features.rawinput;

import cc.polyfrost.oneconfig.libs.universal.UDesktop;
import club.sk1er.patcher.Patcher;
import club.sk1er.patcher.config.PatcherConfig;
import club.sk1er.patcher.hooks.MouseHelperHook;
import club.sk1er.patcher.util.rawinput.RawInputThread;
import net.minecraft.util.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MouseHelperMixin implements MouseHelperHook {
    @Shadow public int deltaX;
    @Shadow public int deltaY;
    @Unique
    private final RawInputThread polyfrost$rawInput = new RawInputThread();
    @Inject(method = "<init>", at = @At("RETURN"))
    private void startRawInputThread(CallbackInfo ci) {
        if (UDesktop.isWindows()) {
            polyfrost$rawInput.start();
        } else {
            polyfrost$rawInput.disableRawInput(true);
        }
    }

    @Inject(method = "grabMouseCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MouseHelper;deltaX:I"))
    private void grabRawInputCursor(CallbackInfo ci) {
        polyfrost$rawInput.setDx(0);
        polyfrost$rawInput.setDy(0);
    }

    @Inject(method = "mouseXYChange", at = @At("HEAD"), cancellable = true)
    private void rawInputMouseXYChange(CallbackInfo ci) {
        if (PatcherConfig.rawInput && UDesktop.isWindows() && Patcher.instance.getPatcherConfig().enabled) {
            deltaX = (int) polyfrost$rawInput.getDx();
            deltaY = -((int) polyfrost$rawInput.getDy());
            ci.cancel();
        }
    }

    @Override
    public RawInputThread getPolyfrost$rawInput() {
        return polyfrost$rawInput;
    }
}
