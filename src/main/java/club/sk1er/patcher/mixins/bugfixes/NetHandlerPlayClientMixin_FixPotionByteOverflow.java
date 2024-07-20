package club.sk1er.patcher.mixins.bugfixes;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin_FixPotionByteOverflow {

    @Unique private static final String patcher$CONSTRUCTOR =
        //#if MC==10809
        "(IIIZZ)Lnet/minecraft/potion/PotionEffect;";
        //#else
        //$$ "(Lnet/minecraft/potion/Potion;IIZZ)Lnet/minecraft/potion/PotionEffect;";
        //#endif

    //TODO this is a horrible mixin, replace with mixinextras equivalent when it's available
    /**
     * Minecraft allows any potion amplifier number. Hence, when the server packet casts the amplifier to a byte, it
     * overflows and wraps into a negative number. This mixin fixes this issue.
     */
    @Redirect(method = "handleEntityEffect", at = @At(value = "NEW", target = patcher$CONSTRUCTOR))
    private PotionEffect patcher$fixPotionByteOverflow(
        //#if MC==10809
        int potion,
        //#else
        //$$ net.minecraft.potion.Potion potion,
        //#endif
        int effectDuration, int effectAmplifier, boolean ambient, boolean showParticles) {
        return new PotionEffect(potion, effectDuration, effectAmplifier & 0xFF, ambient, showParticles);
    }
}
