package io.github.bakedlibs.dough.common;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

/**
 * A small, in-house compatibility layer that replaces the discontinued
 * <a href="https://github.com/PaperMC/PaperLib">PaperLib</a> library.
 * <p>
 * It detects the server software at runtime (Paper, Spigot or CraftBukkit) and only
 * uses Paper-specific APIs when they are actually available. On Spigot and CraftBukkit
 * it falls back to plain Bukkit APIs, so Slimefun keeps running on all three platforms.
 *
 * @author Slimefun
 */
public final class ServerCompat {

    /**
     * Whether the server is Paper (or a Paper fork such as Purpur).
     */
    private static final boolean PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");

    /**
     * Whether the server exposes the Spigot API (this includes Paper).
     */
    private static final boolean SPIGOT = hasClass("org.spigotmc.SpigotConfig");

    private ServerCompat() {}

    /**
     * @return Whether the server software is Paper (or a Paper fork).
     */
    public static boolean isPaper() {
        return PAPER;
    }

    /**
     * @return Whether the server software exposes the Spigot API (true for Spigot and Paper).
     */
    public static boolean isSpigot() {
        return SPIGOT;
    }

    /**
     * Returns a {@link BlockState} for the given {@link Block}.
     * <p>
     * On Paper this can optionally return a non-snapshot state, which is cheaper and
     * writes through to the world immediately. On Spigot and CraftBukkit a real snapshot
     * is returned instead and {@link BlockStateSnapshot#isSnapshot()} reports {@code true},
     * so callers know they have to {@link BlockState#update(boolean, boolean) update} it.
     *
     * @param block
     *            The {@link Block} to read
     * @param useSnapshot
     *            Whether a snapshot should be used when the server supports it
     *
     * @return A {@link BlockStateSnapshot} wrapping the resulting {@link BlockState}
     */
    public static @Nonnull BlockStateSnapshot getBlockState(@Nonnull Block block, boolean useSnapshot) {
        if (PAPER) {
            return new BlockStateSnapshot(useSnapshot, block.getState(useSnapshot));
        }

        return new BlockStateSnapshot(true, block.getState());
    }

    /**
     * Teleports an {@link Entity} to the target {@link Location}, loading the chunk
     * asynchronously first if the server supports it.
     *
     * @param entity
     *            The {@link Entity} to teleport
     * @param destination
     *            The {@link Location} to teleport to
     *
     * @return A {@link CompletableFuture} that completes with the result of the teleport
     */
    public static @Nonnull CompletableFuture<Boolean> teleportAsync(@Nonnull Entity entity, @Nonnull Location destination) {
        return teleportAsync(entity, destination, TeleportCause.PLUGIN);
    }

    /**
     * Teleports an {@link Entity} to the target {@link Location}, loading the chunk
     * asynchronously first if the server supports it.
     *
     * @param entity
     *            The {@link Entity} to teleport
     * @param destination
     *            The {@link Location} to teleport to
     * @param cause
     *            The {@link TeleportCause} of the teleportation
     *
     * @return A {@link CompletableFuture} that completes with the result of the teleport
     */
    public static @Nonnull CompletableFuture<Boolean> teleportAsync(@Nonnull Entity entity, @Nonnull Location destination, @Nonnull TeleportCause cause) {
        if (PAPER) {
            return entity.teleportAsync(destination, cause);
        }

        return CompletableFuture.completedFuture(entity.teleport(destination, cause));
    }

    /**
     * Prints a friendly message advising the server owner to switch to Paper.
     * This does nothing if the server is already running Paper.
     *
     * @param plugin
     *            The {@link Plugin} asking for the recommendation
     */
    public static void suggestPaper(@Nonnull Plugin plugin) {
        if (PAPER) {
            return;
        }

        Logger logger = plugin.getLogger();
        String pluginName = plugin.getDescription().getName();

        logger.log(Level.INFO, "====================================================");
        logger.log(Level.INFO, " {0} works better if you use Paper as your server software.", pluginName);

        if (System.getProperty("slimefun.paper-recommendation-shown") == null) {
            System.setProperty("slimefun.paper-recommendation-shown", "1");
            logger.log(Level.INFO, " Paper offers significant performance improvements, bug fixes and security enhancements.");
            logger.log(Level.INFO, " Join the Paper Community @ https://papermc.io");
        }

        logger.log(Level.INFO, "====================================================");
    }

    private static boolean hasClass(@Nonnull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException x) {
            return false;
        }
    }

    /**
     * A reusable result holder describing a {@link BlockState} and whether it is a snapshot.
     */
    public static final class BlockStateSnapshot {

        private final boolean snapshot;
        private final BlockState state;

        public BlockStateSnapshot(boolean snapshot, @Nonnull BlockState state) {
            this.snapshot = snapshot;
            this.state = state;
        }

        /**
         * @return Whether the {@link BlockState} is a snapshot and thus needs to be
         *         {@link BlockState#update(boolean, boolean) updated} manually
         */
        public boolean isSnapshot() {
            return snapshot;
        }

        /**
         * @return The resulting {@link BlockState}
         */
        public @Nonnull BlockState getState() {
            return state;
        }
    }
}
