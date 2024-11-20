package club.sk1er.patcher;

import cc.polyfrost.oneconfig.libs.universal.UDesktop;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.NetworkUtils;
import cc.polyfrost.oneconfig.utils.Notifications;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import club.sk1er.patcher.asm.render.screen.GuiChatTransformer;
import club.sk1er.patcher.commands.PatcherCommand;
import club.sk1er.patcher.config.PatcherConfig;
import club.sk1er.patcher.config.PatcherSoundConfig;
import club.sk1er.patcher.ducks.FontRendererExt;
import club.sk1er.patcher.hooks.EntityRendererHook;
import club.sk1er.patcher.hooks.MinecraftHook;
import club.sk1er.patcher.mixins.features.network.packet.C01PacketChatMessageMixin_ExtendedChatLength;
import club.sk1er.patcher.render.ScreenshotPreview;
import club.sk1er.patcher.screen.PatcherMenuEditor;
import club.sk1er.patcher.screen.render.caching.HUDCaching;
import club.sk1er.patcher.screen.render.overlay.ArmorStatusRenderer;
import club.sk1er.patcher.screen.render.overlay.GlanceRenderer;
import club.sk1er.patcher.screen.render.overlay.ImagePreview;
import club.sk1er.patcher.screen.render.overlay.metrics.MetricsRenderer;
import club.sk1er.patcher.screen.render.title.TitleFix;
import club.sk1er.patcher.tweaker.PatcherTweaker;
import club.sk1er.patcher.util.chat.ChatHandler;
import club.sk1er.patcher.util.enhancement.EnhancementManager;
import club.sk1er.patcher.util.enhancement.ReloadListener;
import club.sk1er.patcher.util.forge.EntrypointCaching;
import club.sk1er.patcher.util.fov.FovHandler;
import club.sk1er.patcher.util.keybind.FunctionKeyChanger;
import club.sk1er.patcher.util.keybind.KeybindDropModifier;
import club.sk1er.patcher.util.keybind.MousePerspectiveKeybindHandler;
import club.sk1er.patcher.util.keybind.linux.LinuxKeybindFix;
import club.sk1er.patcher.util.screenshot.AsyncScreenshots;
import club.sk1er.patcher.util.status.ProtocolVersionDetector;
import club.sk1er.patcher.util.world.SavesWatcher;
import club.sk1er.patcher.util.world.render.culling.EntityCulling;
import club.sk1er.patcher.util.world.render.entity.EntityRendering;
import club.sk1er.patcher.util.world.sound.SoundHandler;
import club.sk1er.patcher.util.world.sound.audioswitcher.AudioSwitcher;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Mod(modid = "@ID@", name = "@NAME@", version = Patcher.VERSION, clientSideOnly = true)
public class Patcher {

    @Mod.Instance("patcher")
    public static Patcher instance;

    // normal versions will be "1.x.x"
    // betas will be "1.x.x+beta-y" / "1.x.x+branch_beta-y"
    // rcs will be 1.x.x+rc-y
    // extra branches will be 1.x.x+branch-y
    public static final String VERSION = "@VER@";

    private final Logger logger = LogManager.getLogger("Patcher");
    private final File logsDirectory = new File(Minecraft.getMinecraft().mcDataDir + File.separator + "logs" + File.separator);

    /**
     * Create a set of blacklisted servers, used for when a specific server doesn't allow for 1.8 clients to use
     * our 1.11 text length modifier (bring message length from 100 to 256, as done in 1.11 and above) {@link Patcher#addOrRemoveBlacklist(String)}.
     */
    private final Set<String> blacklistedServers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final File blacklistedServersFile = new File("./config/blacklisted_servers.txt");

    private final SavesWatcher savesWatcher = new SavesWatcher();
    private final AudioSwitcher audioSwitcher = new AudioSwitcher();

    private KeyBinding dropModifier, hideScreen, customDebug, clearShaders;

    private PatcherConfig patcherConfig;
    private PatcherSoundConfig patcherSoundConfig;

    private boolean loadedGalacticFontRenderer;

    private boolean isEssential;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        EntrypointCaching.INSTANCE.onInit();

        registerKeybinds(
            dropModifier = new KeybindDropModifier(),
            hideScreen = new FunctionKeyChanger.KeybindHideScreen(),
            customDebug = new FunctionKeyChanger.KeybindCustomDebug(),
            clearShaders = new FunctionKeyChanger.KeybindClearShaders()
        );

        patcherConfig = PatcherConfig.INSTANCE;
        patcherSoundConfig = new PatcherSoundConfig(null, null);

        SoundHandler soundHandler = new SoundHandler();
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        resourceManager.registerReloadListener(soundHandler);
        resourceManager.registerReloadListener(new ReloadListener());

        registerCommands(
            new PatcherCommand(),
            new AsyncScreenshots.FavoriteScreenshot(), new AsyncScreenshots.DeleteScreenshot(),
            new AsyncScreenshots.UploadScreenshot(), new AsyncScreenshots.CopyScreenshot(),
            new AsyncScreenshots.ScreenshotsFolder()
        );

        registerEvents(
            this, soundHandler, dropModifier, audioSwitcher,
            new EntityRendering(), new FovHandler(),
            new ChatHandler(), new GlanceRenderer(), new EntityCulling(),
            new ArmorStatusRenderer(), new PatcherMenuEditor(), new ImagePreview(),
            new TitleFix(), new LinuxKeybindFix(),
            new MetricsRenderer(), new HUDCaching(), new EntityRendererHook(),
            MinecraftHook.INSTANCE, ScreenshotPreview.INSTANCE,
            new MousePerspectiveKeybindHandler()
        );

        checkLogs();
        loadBlacklistedServers();
        fixSettings();

        this.savesWatcher.watch();
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        if (!loadedGalacticFontRenderer) {
            loadedGalacticFontRenderer = true;
            FontRenderer galacticFontRenderer = Minecraft.getMinecraft().standardGalacticFontRenderer;
            if (galacticFontRenderer instanceof FontRendererExt) {
                ((FontRendererExt) galacticFontRenderer).patcher$getFontRendererHook().create();
            }
        }
        isEssential = Loader.isModLoaded("essential");
    }

    @EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        List<ModContainer> activeModList = Loader.instance().getActiveModList();
        Notifications notifications = Notifications.INSTANCE;
        this.detectIncompatibilities(activeModList, notifications);
        this.detectReplacements(activeModList, notifications);

        long time = (System.currentTimeMillis() - PatcherTweaker.clientLoadTime);
        if (PatcherConfig.startupNotification) {
            notifications.send("Minecraft Startup", "Minecraft started in " + (time / 1000L) + " seconds.");
        }

        logger.info("Minecraft started in {}ms.", time);

        //noinspection ConstantConditions
        if (!ForgeVersion.mcVersion.equals("1.8.9") || ForgeVersion.getVersion().contains("2318")) return;
        notifications.send("Patcher", "Outdated Forge has been detected (" + ForgeVersion.getVersion() + "). " +
            "Click to open the Forge website to download the latest version.", 30000f, () -> {
            String updateLink = "https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html";
            try {
                UDesktop.browse(URI.create(updateLink));
            } catch (Exception openException) {
                this.logger.error("Failed to open Forge website.", openException);
                notifications.send("Patcher", "Failed to open Forge website. Link is now copied to your clipboard.");
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(updateLink), null);
                } catch (Exception clipboardException) {
                    // there is no hope
                    this.logger.error("Failed to copy Forge website to clipboard.", clipboardException);
                    notifications.send("Patcher", "Failed to copy Forge website to clipboard.");
                }
            }
        });
    }

    /**
     * Runs when the user connects to a server.
     * Goes through the process of checking the current state of the server.
     * <p>
     * If the server is local, return and set the chat length to 256, as we modify the client to allow for
     * 256 message length in singleplayer through Mixins in {@link C01PacketChatMessageMixin_ExtendedChatLength}.
     * <p>
     * If the server is blacklisted, return and set the chat length to 100, as that server does not support 256 long
     * chat messages, and was manually blacklisted by the player.
     * <p>
     * If the server is not local nor blacklisted, check the servers protocol and see if it supports 315, aka 1.11.
     * If it does, then set the message length max to 256, otherwise return to 100.
     *
     * @param event {@link FMLNetworkEvent.ClientConnectedToServerEvent}
     */
    //#if MC==10809
    @SubscribeEvent
    public void connectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (event.isLocal) {
            GuiChatTransformer.maxChatLength = 256;
            return;
        }

        String serverIP = Minecraft.getMinecraft().getCurrentServerData().serverIP;
        if (serverIP == null || blacklistedServers.contains(serverIP)) {
            GuiChatTransformer.maxChatLength = 100;
            return;
        }

        boolean compatible = ProtocolVersionDetector.instance.isCompatibleWithVersion(
            serverIP,
            315 // 1.11
        );

        GuiChatTransformer.maxChatLength = compatible ? 256 : 100;
    }
    //#endif

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) EnhancementManager.getInstance().tick();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkLogs() {
        if (PatcherConfig.logOptimizer && logsDirectory.exists()) {
            File[] files = logsDirectory.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (file.getName().endsWith("log.gz") && file.lastModified() <= (System.currentTimeMillis() - PatcherConfig.logOptimizerLength * 86400000L)) {
                    file.delete();
                }
            }
        }
    }

    private void registerKeybinds(KeyBinding... keybinds) {
        for (KeyBinding keybind : keybinds) {
            ClientRegistry.registerKeyBinding(keybind);
        }
    }

    private void registerEvents(Object... events) {
        for (Object event : events) {
            MinecraftForge.EVENT_BUS.register(event);
        }
    }

    private void registerCommands(Object... commands) {
        for (Object command : commands) {
            CommandManager.register(command);
        }
    }

    private boolean isServerBlacklisted(String ip) {
        if (ip == null) return false;
        String trim = ip.trim();
        return !trim.isEmpty() && blacklistedServers.contains(trim);
    }

    public boolean addOrRemoveBlacklist(String input) {
        if (input == null || input.isEmpty() || input.trim().isEmpty()) {
            return false;
        } else {
            input = input.trim();

            if (isServerBlacklisted(input)) {
                blacklistedServers.remove(input);
                return false;
            } else {
                blacklistedServers.add(input);
                return true;
            }
        }
    }

    public void saveBlacklistedServers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blacklistedServersFile))) {
            File parentFile = blacklistedServersFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                return;
            }

            if (!blacklistedServersFile.exists() && !blacklistedServersFile.createNewFile()) {
                return;
            }

            for (String server : blacklistedServers) {
                writer.write(server + System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("Failed to save blacklisted servers.", e);
        }
    }

    private void loadBlacklistedServers() {
        if (!blacklistedServersFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(blacklistedServersFile))) {
            String servers;

            while ((servers = reader.readLine()) != null) {
                blacklistedServers.add(servers);
            }
        } catch (IOException e) {
            logger.error("Failed to load blacklisted servers.", e);
        }
    }

    private void fixSettings() {
        if (PatcherConfig.fireOverlayHeight < -0.5F || PatcherConfig.fireOverlayHeight > 1.5F) {
            PatcherConfig.fireOverlayHeight = 0.0F;
        }
        if (PatcherConfig.customZoomSensitivity > 1.0F) PatcherConfig.customZoomSensitivity = 1.0F;
        if (PatcherConfig.imagePreviewWidth > 1.0F) PatcherConfig.imagePreviewWidth = 0.5F;
        if (PatcherConfig.previewScale > 1.0F) PatcherConfig.previewScale = 1.0F;
        if (PatcherConfig.unfocusedFPSAmount < 10) PatcherConfig.unfocusedFPSAmount = 10;

        this.forceSaveConfig();
    }

    private void detectIncompatibilities(List<ModContainer> activeModList, Notifications notifications) {
        for (ModContainer container : activeModList) {
            String modId = container.getModId();
            String baseMessage = container.getName() + " has been detected. ";
            if (PatcherConfig.entityCulling && modId.equals("enhancements")) {
                notifications.send("Patcher", baseMessage + "Entity Culling is now disabled.");
                PatcherConfig.entityCulling = false;
            }

            if ((modId.equals("labymod") || modId.equals("enhancements")) || modId.equals("hychat")) {
                if (PatcherConfig.compactChat) {
                    notifications.send("Patcher", baseMessage + "Compact Chat is now disabled.");
                    PatcherConfig.compactChat = false;
                }
            }

            if (PatcherConfig.optimizedFontRenderer && modId.equals("smoothfont")) {
                notifications.send("Patcher", baseMessage + "Optimized Font Renderer is now disabled.");
                PatcherConfig.optimizedFontRenderer = false;
            }
        }

        try {
            Class.forName("net.labymod.addons.resourcepacks24.Resourcepacks24", false, getClass().getClassLoader());
            notifications.send("Patcher", "The LabyMod addon \"Resourcepacks24\" conflicts with Patcher's resourcepack optimizations. Please remove it to make it work again.");
        } catch (ClassNotFoundException ignored) {

        }

        this.forceSaveConfig();
    }

    private void detectReplacements(List<ModContainer> activeModList, Notifications notifications) {
        Multithreading.runAsync(() -> {
            JsonObject replacedMods;
            try { // todo: replaced an async thing but i think its fine because get() pauses the game thread anyways i think???
                replacedMods = NetworkUtils.getJsonElement("https://static.sk1er.club/patcher/duplicate_mods.json").getAsJsonObject();
            } catch (Exception e) {
                logger.error("Failed to fetch list of replaced mods at \"https://static.sk1er.club/patcher/duplicate_mods.json\".", e);
                return;
            }

            if (replacedMods == null) return;
            Set<String> replacements = new HashSet<>();
            Set<String> modids = replacedMods.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
            for (ModContainer modContainer : activeModList) {
                if (modids.contains(modContainer.getModId())) {
                    replacements.add(modContainer.getName());
                }
            }

            if (!replacements.isEmpty()) {
                for (String replacement : replacements) {
                    if (replacement.equals("Clean View")) {
                        notifications.send("PolyPatcher", replacement + " can be removed as it is replaced by OverflowParticles. Click here to download OverflowParticles", 6f, () -> UDesktop.browse(URI.create("https://modrinth.com/mod/overflowparticles")));
                        continue;
                    }
                    notifications.send("PolyPatcher", replacement + " can be removed as it is replaced by PolyPatcher.", 6f);
                }
            }
        });
    }

    public PatcherConfig getPatcherConfig() {
        return patcherConfig;
    }

    public PatcherSoundConfig getPatcherSoundConfig() {
        return patcherSoundConfig;
    }

    public Logger getLogger() {
        return logger;
    }

    public KeyBinding getDropModifier() {
        return dropModifier;
    }

    public KeyBinding getHideScreen() {
        return hideScreen;
    }

    public KeyBinding getCustomDebug() {
        return customDebug;
    }

    public KeyBinding getClearShaders() {
        return clearShaders;
    }

    public AudioSwitcher getAudioSwitcher() {
        return audioSwitcher;
    }

    public void forceSaveConfig() {
        this.patcherConfig.save();
    }

    public boolean isEssential() {
        return isEssential;
    }
}
