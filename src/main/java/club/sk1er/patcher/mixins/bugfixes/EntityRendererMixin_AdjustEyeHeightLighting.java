package club.sk1er.patcher.mixins.bugfixes;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin_AdjustEyeHeightLighting {

    @Redirect(method = "updateRenderer", at = @At(value = "NEW", target = "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/BlockPos;"))
    private BlockPos patcher$accountForEyes(Entity source) {
        return new BlockPos(source.getPositionEyes(1.0F));
    }

}
