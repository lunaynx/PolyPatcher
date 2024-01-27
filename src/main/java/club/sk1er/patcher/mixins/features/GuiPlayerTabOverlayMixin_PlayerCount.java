package club.sk1er.patcher.mixins.features;

import club.sk1er.patcher.config.PatcherConfig;
import com.google.common.collect.Ordering;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin_PlayerCount {
    @Shadow
    @Final
    private static Ordering<NetworkPlayerInfo> field_175252_a;

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 80))
    private int patcher$changePlayerCount(int original) {
        return PatcherConfig.tabPlayerCount;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE"), ordinal = 0)
    private List<NetworkPlayerInfo> setLimit(List<NetworkPlayerInfo> value) {
        return value.subList(0, Math.min(value.size(), PatcherConfig.tabPlayerCount));
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1))
    private int noLimit(int a, int b) {
        return a;
    }
}
