package club.sk1er.patcher.mixins.bugfixes.forge;

import
//#if MC==10809
    com.google.common.base.Optional;
//#else
//$$ java.util.Optional;
//#endif

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ItemLayerModel;

//#if MC==10809
import net.minecraftforge.client.model.
//#else
//$$ import net.minecraftforge.common.model.
//#endif
    TRSRTransformation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemLayerModel.class, remap = false)
public abstract class ItemLayerModelMixin_HeldItemLighting {

    @Unique private static EnumFacing patcher$side;

    @Inject(method = "buildSideQuad", at = @At(value = "HEAD"))
    private static void patcher$captureSide(VertexFormat format, Optional<TRSRTransformation> transform, EnumFacing side, int tint, TextureAtlasSprite sprite, int u, int v, CallbackInfoReturnable<BakedQuad> cir) {
        patcher$side = side;
    }

    @ModifyArg(method = "buildSideQuad", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/model/ItemLayerModel;buildQuad(Lnet/minecraft/client/renderer/vertex/VertexFormat;Lcom/google/common/base/Optional;Lnet/minecraft/util/EnumFacing;IFFFFFFFFFFFFFFFFFFFF)Lnet/minecraft/client/renderer/block/model/BakedQuad;"), index = 2, remap = false)
    private static EnumFacing patcher$correctLighting(EnumFacing original) {
        return patcher$side.getAxis() == EnumFacing.Axis.Y ? patcher$side.getOpposite() : patcher$side;
    }

}
