package club.sk1er.patcher.mixins.performance.forge;

import club.sk1er.patcher.util.forge.EntrypointCaching;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;

@Mixin(value = JarDiscoverer.class, remap = false)
public class JarDiscovererMixin_EntrypointCaching {

    @Unique
    private static final String patcher$discoverCachedEntrypointsTarget =
        //#if MC==10809
        "Ljava/util/jar/JarFile;entries()Ljava/util/Enumeration;";
        //#else
        //$$ "Ljava/util/jar/JarFile;getEntry(Ljava/lang/String;)Ljava/util/zip/ZipEntry;";
        //#endif

    @Unique
    private static final String patcher$putCachedEntrypointsMethod =
        //#if MC==10809
        "discover";
        //#else
        //$$ "findClassesASM";
        //#endif

    @Unique
    private static final String patcher$putCachedEntrypointsTarget =
        //#if MC==10809
        "Lnet/minecraftforge/fml/common/ModContainer;bindMetadata(Lnet/minecraftforge/fml/common/MetadataCollection;)V";
        //#else
        //$$ "Lnet/minecraftforge/fml/common/ModContainer;setClassVersion(I)V";
        //#endif

    //#if MC==11202
    //$$ @org.spongepowered.asm.mixin.Shadow
    //$$ @org.spongepowered.asm.mixin.Final
    //$$ private static boolean ENABLE_JSON_TEST;
    //#endif

    @Inject(method = "discover", at = @At(value = "INVOKE", target = patcher$discoverCachedEntrypointsTarget), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void patcher$discoverCachedEntrypoints(ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable<List<ModContainer>> cir, List<ModContainer> foundMods, JarFile jar, ZipEntry modInfo, MetadataCollection mc) {
        //#if MC==11202
        //$$ if (ENABLE_JSON_TEST) return;
        //#endif
        List<ModContainer> cachedEntrypoints = EntrypointCaching.INSTANCE.discoverCachedEntrypoints(candidate, table, jar, mc);
        if (cachedEntrypoints != null) {
            cir.setReturnValue(cachedEntrypoints);
        }
    }

    @Inject(method = patcher$putCachedEntrypointsMethod, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/discovery/ModCandidate;addClassEntry(Ljava/lang/String;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void patcher$putCachedClassEntries(
        //#if MC==10809
        ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable<List<ModContainer>> cir, List<ModContainer> foundMods, JarFile jar, ZipEntry modInfo, MetadataCollection mc, Iterator<ZipEntry> var7, ZipEntry ze, Matcher var9
        //#else
        //$$ ModCandidate candidate, ASMDataTable table, JarFile jar, List<ModContainer> foundMods, MetadataCollection mc, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci, Iterator<ZipEntry> var6, ZipEntry ze, Matcher match, net.minecraftforge.fml.common.discovery.asm.ASMModParser modParser, ModContainer container
        //#endif
    ) {
        EntrypointCaching.INSTANCE.putCachedClassEntries(candidate, ze);
    }

    @Inject(method = patcher$putCachedEntrypointsMethod, at = @At(value = "INVOKE", target = patcher$putCachedEntrypointsTarget, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void patcher$putCachedEntrypoints(
        //#if MC==10809
        ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable<List<ModContainer>> cir, List<ModContainer> foundMods, JarFile jar, ZipEntry modInfo, MetadataCollection mc, Iterator<ZipEntry> var7, ZipEntry ze, Matcher var9
        //#else
        //$$ ModCandidate candidate, ASMDataTable table, JarFile jar, List<ModContainer> foundMods, MetadataCollection mc, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci, Iterator<ZipEntry> var6, ZipEntry ze, Matcher match, net.minecraftforge.fml.common.discovery.asm.ASMModParser modParser, ModContainer container
        //#endif
    ) {
        EntrypointCaching.INSTANCE.putCachedEntrypoints(candidate, ze);
    }
}
