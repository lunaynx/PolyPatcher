package club.sk1er.patcher.hooks;

import club.sk1er.patcher.config.PatcherConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
//#if MC==11202
//$$ import net.minecraft.block.state.IBlockState;
//#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@SuppressWarnings("unused")
public class EntityRendererHook {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean isBeingHeld = false;
    private static float oldSensitivity;
    private static float partialTicks;
    public static float lastZoomModifier;

    public static boolean hasMap() {
        if (!PatcherConfig.mapBobbing || mc.thePlayer == null) return false;
        //#if MC==10809
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemMap;
        //#else
        //$$ ItemStack mainHandItem = mc.player.getHeldItemMainhand();
        //$$ ItemStack offHandItem = mc.player.getHeldItemOffhand();
        //$$ return (mainHandItem != null && mainHandItem.getItem() instanceof ItemMap) || (offHandItem != null && offHandItem.getItem() instanceof ItemMap);
        //#endif
    }

    @SubscribeEvent
    public void worldRender(RenderWorldLastEvent event) {
        //#if MC==10809
        partialTicks = event.partialTicks;
        //#else
        //$$ partialTicks = event.getPartialTicks();
        //#endif
    }
}
