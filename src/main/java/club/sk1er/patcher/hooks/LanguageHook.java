package club.sk1er.patcher.hooks;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.LanguageManager;

import java.util.List;

public class LanguageHook {
    private static boolean loadLanguage = false;
    private static List<IResourceManagerReloadListener> languageManager = null;

    public static boolean shouldLoadLanguage() {
        return loadLanguage;
    }

    public static List<IResourceManagerReloadListener> getLanguageManager() {
        return languageManager;
    }

    public static void setLanguageManager(IResourceManagerReloadListener languageManager) {
        LanguageHook.languageManager = Lists.newArrayList(languageManager);
    }

    public static void setLoadLanguage(boolean loadLanguage) {
        LanguageHook.loadLanguage = loadLanguage;
    }
}
