package io.github.bakedlibs.dough.versions;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * This is an extension of {@link SemanticVersion}, specifically designed
 * for Minecraft's versioning system.
 * <p>
 * This class is a patched copy of dough's {@code MinecraftVersion}. Since Minecraft
 * switched to their year-based versioning scheme ("26.1") in 2026, the original
 * implementation, which could only parse semantic versions such as {@literal "1.21.4"},
 * failed on the upcoming version strings (for example {@literal "26.3.build.169-beta"}).
 * This copy supports both the legacy {@literal "1.<minor>.<patch>"} scheme and the new
 * {@literal "<year>.<drop>.<hotfix>"} scheme.
 * <p>
 * The class is deliberately kept in dough's original package so that the
 * {@link #of(Server)} method can construct dough's package-private
 * {@link UnknownServerVersionException}. The maven-shade-plugin will relocate
 * this class (and its references) together with the rest of dough and the
 * original dough class is excluded from the shaded jar.
 *
 * @author TheBusyBiscuit
 *
 * @see SemanticVersion
 *
 */
public class MinecraftVersion extends SemanticVersion {

    /**
     * This method constructs a new {@link MinecraftVersion} with the given
     * version components.
     *
     * @param major
     *            The "major" version (according to semver)
     * @param minor
     *            The "minor" version (according to semver)
     * @param patch
     *            The "patch" version (according to semver)
     */
    public MinecraftVersion(int major, int minor, int patch) {
        super(major, minor, patch);
    }

    /**
     * This attempts to get the {@link MinecraftVersion} on which the given {@link Server}
     * is currently running on.
     *
     * @param server
     *            The {@link Server} instance
     *
     * @return The current {@link MinecraftVersion}
     *
     * @throws UnknownServerVersionException
     *             This exception is thrown when the {@link Server} version could not be identified
     */
    public static @Nonnull MinecraftVersion of(@Nonnull Server server) throws UnknownServerVersionException {
        Validate.notNull(server, "Server should not be null!");
        String bukkitVersion = server.getBukkitVersion();

        try {
            // Strip away the build suffix, e.g. "-R0.1-SNAPSHOT" or "-beta"
            String version = bukkitVersion.split("-")[0];
            String[] parts = version.split("\\.");

            int major;
            int minor;
            int patch = 0;

            if (parts[0].equals("1")) {
                // Legacy versioning scheme: "1.<minor>[.<patch>]"
                major = 1;
                minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

                if (parts.length > 2) {
                    patch = Integer.parseInt(parts[2]);
                }
            } else {
                // Year-based versioning scheme (2026+): "<year>.<drop>[.<hotfix>]"
                // The third component may also be non-numeric build metadata
                // (for example "26.3.build.169"), which we simply ignore.
                major = Integer.parseInt(parts[0]);
                minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

                if (parts.length > 2) {
                    try {
                        patch = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException ignored) {
                        // Not a hotfix number, just ignore it.
                    }
                }
            }

            return new MinecraftVersion(major, minor, patch);
        } catch (Exception x) {
            // Something failed.
            throw new UnknownServerVersionException(bukkitVersion, x);
        }
    }

    /**
     * This attempts to get the {@link MinecraftVersion} on which the current {@link Server}
     * is running on.
     *
     * @return The current {@link MinecraftVersion}
     *
     * @throws UnknownServerVersionException
     *             This exception is thrown when the {@link Server} version could not be identified
     */
    public static @Nonnull MinecraftVersion get() throws UnknownServerVersionException {
        return of(Bukkit.getServer());
    }

    /**
     * This checks if the current Server instance is a mock (MockBukkit) and
     * whether we are in a Unit Test environment.
     *
     * @param server
     *            The {@link Server} implementation to investigate
     *
     * @return Whether the current Server instance is a mock
     */
    public static boolean isMocked(@Nonnull Server server) {
        /**
         * We start our search with the current Server class.
         */
        Class<?> clazz = server.getClass();

        /*
         * Loop through the entire class hierarchy.
         * As of MockBukkit-1.15 v0.30 MockBukkit allows you to
         * pass your custom ServerMock class into MockBukkit.mock().
         *
         * That's why checking the Server implementation class alone isn't
         * enough, the custom class could be located anywhere and named anything.
         * So we start iterating through the entire class hierarchy to see
         * if we find our ServerMock class within this hierarchy.
         */
        while (clazz != null) {
            // We only check the last part here in case the package name changes.
            if (clazz.getName().endsWith("mockbukkit.ServerMock")) {
                // We found our MockBukkit class
                return true;
            } else {
                // Advance to the next node
                clazz = clazz.getSuperclass();
            }
        }

        // No MockBukkit classes present in the type hierarchy.
        return false;
    }

    /**
     * This checks if the current Server instance is a mock (MockBukkit) and
     * whether we are in a Unit Test environment.
     *
     * @return Whether the current Server instance is a mock
     */
    public static boolean isMocked() {
        return isMocked(Bukkit.getServer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull String getAsString() {
        return "Minecraft " + super.getAsString();
    }

}
