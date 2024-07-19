package club.sk1er.patcher.util;

import club.sk1er.patcher.Patcher;
import club.sk1er.patcher.util.chat.ChatUtilities;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongLongMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kbrewster.eventbus.forge.KEventBus;
import me.kbrewster.eventbus.forge.invokers.LMFInvoker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class EventBusBenchmark {
    private static class EventBusTestEvent extends Event {}
    private static class EventBusTest {
        @SubscribeEvent
        public void testEvent(EventBusTestEvent event) {

        }
    }

    public static void benchmark() {
        int runs = 5;
        List<String> messages = new ArrayList<>(runs * 6);
        List<Pair<Long, Long>> registerTimes = new ArrayList<>(runs);
        List<Pair<Long, Long>> postTimes = new ArrayList<>(runs);
        List<Pair<Long, Long>> unregisterTimes = new ArrayList<>(runs);

        for (int i = 0; i < runs; i++) {
            Pair<Long, Long> registerTime = LongLongMutablePair.of(0L, 0L);
            Pair<Long, Long> postTime = LongLongMutablePair.of(0L, 0L);
            Pair<Long, Long> unregisterTime = LongLongMutablePair.of(0L, 0L);
            Patcher.instance.getLogger().info("Running test " + (i + 1) + " of " + runs);
            runTest(messages, registerTime, postTime, unregisterTime);
            Patcher.instance.getLogger().info("Done with test " + (i + 1) + " of " + runs);
            registerTimes.add(registerTime);
            postTimes.add(postTime);
            unregisterTimes.add(unregisterTime);
        }

        long totalRegisterKEventBus = 0;
        long totalRegisterForge = 0;
        long totalPostKEventBus = 0;
        long totalPostForge = 0;
        long totalUnregisterKEventBus = 0;
        long totalUnregisterForge = 0;

        for (Pair<Long, Long> time : registerTimes) {
            totalRegisterKEventBus += time.first();
            totalRegisterForge += time.second();
        }

        for (Pair<Long, Long> time : postTimes) {
            totalPostKEventBus += time.first();
            totalPostForge += time.second();
        }

        for (Pair<Long, Long> time : unregisterTimes) {
            totalUnregisterKEventBus += time.first();
            totalUnregisterForge += time.second();
        }

        String message1 = "Average time to register 10,000 listeners: " + (totalRegisterKEventBus / (double) runs) + "ms for KEventBus, " + (totalRegisterForge / (double) runs) + "ms for Forge";
        String message2 = "Average time to post 1,000 events: " + (totalPostKEventBus / (double) runs) + "ms for KEventBus, " + (totalPostForge / (double) runs) + "ms for Forge";
        String message3 = "Average time to unregister 10,000 listeners: " + (totalUnregisterKEventBus / (double) runs) + "ms for KEventBus, " + (totalUnregisterForge / (double) runs) + "ms for Forge";

        Patcher.instance.getLogger().info(message1);
        Patcher.instance.getLogger().info(message2);
        Patcher.instance.getLogger().info(message3);

        messages.add(message1);
        messages.add(message2);
        messages.add(message3);

        for (String message : messages) {
            ChatUtilities.sendMessage(message);
        }
    }

    private static void runTest(List<String> messages, Pair<Long, Long> registerTimes, Pair<Long, Long> postTimes, Pair<Long, Long> unregisterTimes) {
        ObjectArrayList<EventBusTest> keventbusListeners = new ObjectArrayList<>(10_000);
        ObjectArrayList<EventBusTest> forgeListeners = new ObjectArrayList<>(10_000);
        final KEventBus patcher$kEventBus = new KEventBus(new LMFInvoker(), e -> System.err.println("An exception occurred in a method: " + e.getMessage()));

        long start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) {
            EventBusTest listener = new EventBusTest();
            patcher$kEventBus.register(listener);
            keventbusListeners.add(listener);
        }
        long total1 = (System.nanoTime() - start) / 1_000_000;
        registerTimes.first(total1);
        String message1 = "Registering 10,000 listeners took " + total1 + "ms for KEventBus";
        Patcher.instance.getLogger().info(message1);
        messages.add(message1);

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) {
            EventBusTest listener = new EventBusTest();
            MinecraftForge.EVENT_BUS.register(new EventBusTest());
            forgeListeners.add(listener);
        }
        long total2 = (System.nanoTime() - start) / 1_000_000;
        registerTimes.second(total2);
        String message3 = "Registering 10,000 listeners took " + total2 + "ms for Forge";
        Patcher.instance.getLogger().info(message3);
        messages.add(message3);

        start = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            patcher$kEventBus.post(new EventBusTestEvent());
        }
        long total3 = (System.nanoTime() - start) / 1_000_000;
        postTimes.first(total3);
        String message5 = "Posting 1,000 events took " + total3 + "ms for KEventBus";
        Patcher.instance.getLogger().info(message5);
        messages.add(message5);

        start = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            MinecraftForge.EVENT_BUS.post(new EventBusTestEvent());
        }
        long total4 = (System.nanoTime() - start) / 1_000_000;
        postTimes.second(total4);
        String message6 = "Posting 1,000 events took " + total4 + "ms for Forge";
        Patcher.instance.getLogger().info(message6);
        messages.add(message6);

        start = System.nanoTime();
        for (EventBusTest listener : keventbusListeners) {
            patcher$kEventBus.unregister(listener);
        }
        long total5 = (System.nanoTime() - start) / 1_000_000;
        unregisterTimes.first(total5);
        String message2 = "Unregistering 10,000 listeners took " + total5 + "ms for KEventBus";
        Patcher.instance.getLogger().info(message2);
        messages.add(message2);

        start = System.nanoTime();
        for (EventBusTest listener : forgeListeners) {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        long total6 = (System.nanoTime() - start) / 1_000_000;
        unregisterTimes.second(total6);
        String message4 = "Unregistering 10,000 listeners took " + total6 + "ms for Forge";
        Patcher.instance.getLogger().info(message4);
        messages.add(message4);

        keventbusListeners.clear(); // make sure we don't leak memory here
        forgeListeners.clear();
    }
}
