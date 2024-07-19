package club.sk1er.patcher.mixins.performance.forge;

import me.kbrewster.eventbus.forge.KEventBus;
import me.kbrewster.eventbus.forge.invokers.DirectInvoker;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.*;

@Mixin(value = EventBus.class, remap = false, priority = 0)
public class EventBusMixin_UseKeventBus {
    @Shadow
    @Final
    private int busID;
    @Unique
    private final KEventBus patcher$kEventBus = new KEventBus(new DirectInvoker(), e -> System.err.println("An exception occurred in a method: " + e.getMessage()), false, busID);

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
