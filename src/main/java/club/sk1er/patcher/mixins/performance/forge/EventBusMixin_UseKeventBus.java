package club.sk1er.patcher.mixins.performance.forge;

import me.kbrewster.eventbus.forge.KEventBus;
import me.kbrewster.eventbus.forge.invokers.DirectInvoker;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;

@Mixin(value = EventBus.class, remap = false, priority = 0)
public class EventBusMixin_UseKeventBus {
    @Shadow
    @Final
    private int busID;
    @Unique
    private final Logger patcher$logger = LogManager.getLogger();
    @Unique
    private final KEventBus patcher$kEventBus = new KEventBus(new DirectInvoker(), e -> {
        e.printStackTrace();
        patcher$logger.error("Failed to post event while using KEventBus added by PolyPatcher!");
        patcher$logger.error("Exception: ", e);
        patcher$logger.error("This is likely not a KEventBus issue, but rather an issue with the event itself.");
        patcher$logger.error("To disable KEventBus and use the default debug logging from Forge, set the system property 'patcher.keventbus' to 'false'!");
        patcher$logger.error("Alternatively, contact us at https://polyfrost.org/discord");
    }, false, busID);

    /**
     * @author wyvest
     * @reason Use KEventBus instead of the default EventBus
     */
    @Overwrite
    public void register(Object target) {
        patcher$kEventBus.register(target);
    }

    /**
     * @author wyvest
     * @reason Use KEventBus instead of the default EventBus
     */
    @Overwrite
    public void unregister(Object target) {
        patcher$kEventBus.unregister(target);
    }

    /**
     * @author wyvest
     * @reason Use KEventBus instead of the default EventBus
     */
    @Overwrite
    public boolean post(Event event) {
        patcher$kEventBus.post(event);
        return event.isCancelable() && event.isCanceled();
    }
}
