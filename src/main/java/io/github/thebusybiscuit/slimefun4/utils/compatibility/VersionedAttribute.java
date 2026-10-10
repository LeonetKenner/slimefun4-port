package io.github.thebusybiscuit.slimefun4.utils.compatibility;

import javax.annotation.Nonnull;

import org.bukkit.attribute.Attribute;

/**
 * A utility class for handling the renamed {@link Attribute} constants.
 * <p>
 * In newer versions of Minecraft (26.x), the {@code GENERIC_} prefix was removed
 * from most {@link Attribute} constants, for example {@code GENERIC_MAX_HEALTH}
 * became {@code MAX_HEALTH}. Since we compile against an older API, we cannot
 * reference the new constants directly and resolve them reflectively instead.
 *
 * @author Slimefun
 */
public class VersionedAttribute {

    public static final Attribute MAX_HEALTH = load("GENERIC_MAX_HEALTH", "MAX_HEALTH");

    private VersionedAttribute() {}

    private static @Nonnull Attribute load(@Nonnull String oldName, @Nonnull String newName) {
        try {
            return (Attribute) Attribute.class.getField(oldName).get(null);
        } catch (ReflectiveOperationException x) {
            try {
                return (Attribute) Attribute.class.getField(newName).get(null);
            } catch (ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }

}
