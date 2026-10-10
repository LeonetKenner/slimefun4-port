package io.github.thebusybiscuit.slimefun4.implementation.tasks;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

/**
 * This class tests the {@link TickerTask}, in particular the thread-safety of its
 * internal "ticking locations" collections (see #4276 / #4173).
 */
class TestTickerTask {

    private static ServerMock server;

    @BeforeAll
    static void load() {
        server = MockBukkit.mock();
    }

    @AfterAll
    static void unload() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Iterating the ticking locations must not throw when they are modified concurrently (#4276)")
    void testConcurrentModification() throws InterruptedException {
        TickerTask task = new TickerTask();
        World world = server.addSimpleWorld("world");

        // Add an initial entry
        task.enableTicker(new Location(world, 0, 0, 0));

        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread mutator = new Thread(() -> {
            try {
                for (int i = 0; i < 50_000; i++) {
                    Location location = new Location(world, i, 0, 0);
                    task.enableTicker(location);
                    task.disableTicker(location);
                }
            } catch (Throwable x) {
                error.compareAndSet(null, x);
            }
        });

        mutator.start();

        try {
            // This mirrors what TickerTask#run() does while ticking
            for (int i = 0; i < 50_000; i++) {
                for (Set<Location> locations : task.getLocations().values()) {
                    for (Location ignored : locations) {
                        // Force the iterator to be used
                        Assertions.assertNotNull(ignored);
                    }
                }
            }
        } catch (Throwable x) {
            error.compareAndSet(null, x);
        }

        mutator.join();

        Assertions.assertNull(error.get(), () -> "Concurrent iteration failed: " + error.get());
    }

    @Test
    @DisplayName("Enabling and disabling a ticker should update the locations accordingly")
    void testEnableDisableTicker() {
        TickerTask task = new TickerTask();
        World world = server.addSimpleWorld("ticker_test_world");
        Location location = new Location(world, 10, 64, 10);

        task.enableTicker(location);
        Assertions.assertTrue(task.getLocations(location.getChunk()).contains(location));

        task.disableTicker(location);
        Assertions.assertFalse(task.getLocations(location.getChunk()).contains(location));
    }

    @Test
    @DisplayName("The read-only location map should not be writable")
    void testReadOnlyMap() {
        TickerTask task = new TickerTask();
        World world = server.addSimpleWorld("readonly_test_world");

        Collection<Set<Location>> values = task.getLocations().values();
        Assertions.assertNotNull(values);

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> task.getLocations().put(null, null));
    }
}
