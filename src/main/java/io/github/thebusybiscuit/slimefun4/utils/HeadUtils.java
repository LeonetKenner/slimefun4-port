package io.github.thebusybiscuit.slimefun4.utils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.bakedlibs.dough.common.CommonPatterns;

/**
 * This class holds utilities for creating and modifying custom player heads.
 * <p>
 * This used to be handled by the {@code dough} library, but that library relied on
 * subclassing {@code com.mojang.authlib.GameProfile}, which became a final class in
 * newer versions of authlib (Minecraft 1.21.9+). This utility instead uses Paper's
 * {@link PlayerProfile} API, which works on every supported version of Minecraft.
 *
 * @author TheBusyBiscuit
 * @author Slimefun
 *
 * @see HeadTexture
 */
public final class HeadUtils {

    private static final String TEXTURE_URL_PREFIX = "http://textures.minecraft.net/texture/";
    private static final String TEXTURES_PROPERTY = "textures";
    private static final String PROFILE_NAME = "Slimefun";
    private static final String SESSION_SERVER_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private HeadUtils() {}

    /**
     * This method turns a texture reference into a base64-encoded texture value.
     * A texture reference is either an already base64-encoded texture value or a
     * texture hash (hexadecimal), pointing to a texture on textures.minecraft.net.
     *
     * @param texture
     *            The texture reference (base64 or hexadecimal hash)
     *
     * @return The base64-encoded texture value
     */
    public static @Nonnull String getBase64Texture(@Nonnull String texture) {
        Preconditions.checkNotNull(texture, "Texture cannot be null");

        if (CommonPatterns.HEXADECIMAL.matcher(texture).matches()) {
            String value = "{\"textures\":{\"SKIN\":{\"url\":\"" + TEXTURE_URL_PREFIX + texture + "\"}}}";
            return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        }

        return texture;
    }

    /**
     * This method creates a custom head {@link ItemStack} with the given texture assigned to it.
     *
     * @param texture
     *            The texture reference (base64 or hexadecimal hash)
     *
     * @return A custom head {@link ItemStack}
     */
    public static @Nonnull ItemStack createHead(@Nonnull String texture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        applySkin(item, texture);
        return item;
    }

    /**
     * This method applies the given texture to an {@link ItemStack}.
     *
     * @param item
     *            The {@link ItemStack} to apply the texture to
     * @param texture
     *            The texture reference (base64 or hexadecimal hash)
     *
     * @return Whether the texture was applied successfully
     */
    public static boolean applySkin(@Nonnull ItemStack item, @Nonnull String texture) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setPlayerProfile(createProfile(texture));
            item.setItemMeta(meta);
            return true;
        }

        return false;
    }

    /**
     * This method applies the given texture to a player head {@link Block}.
     *
     * @param block
     *            The player head {@link Block} to apply the texture to
     * @param texture
     *            The texture reference (base64 or hexadecimal hash)
     * @param update
     *            Whether to update the {@link Block} in the world
     */
    public static void setSkin(@Nonnull Block block, @Nonnull String texture, boolean update) {
        Material type = block.getType();

        if (type != Material.PLAYER_HEAD && type != Material.PLAYER_WALL_HEAD) {
            throw new IllegalArgumentException("Cannot update skin texture of block of type " + type);
        }

        BlockState state = block.getState();

        if (state instanceof Skull skull) {
            skull.setPlayerProfile(createProfile(texture));

            if (update) {
                skull.update(true, false);
            }
        }
    }

    /**
     * This method fetches the skin of the given player from Mojang's session server.
     *
     * @param uuid
     *            The {@link UUID} of the player
     *
     * @return A {@link CompletableFuture} that completes with the base64-encoded
     *         texture value of the player's skin, or completes exceptionally if the
     *         skin could not be fetched
     */
    public static @Nonnull CompletableFuture<String> fetchSkin(@Nonnull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = new URL(SESSION_SERVER_URL + uuid).openStream()) {
                JsonObject response = JsonParser.parseReader(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

                for (var element : response.getAsJsonArray("properties")) {
                    JsonObject property = element.getAsJsonObject();

                    if (TEXTURES_PROPERTY.equals(property.get("name").getAsString())) {
                        return property.get("value").getAsString();
                    }
                }
            } catch (Exception x) {
                throw new java.util.concurrent.CompletionException(x);
            }

            throw new java.util.concurrent.CompletionException(new IllegalStateException("Could not find any textures for profile " + uuid));
        });
    }

    private static @Nonnull PlayerProfile createProfile(@Nonnull String texture) {
        PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)), PROFILE_NAME);
        profile.setProperty(new ProfileProperty(TEXTURES_PROPERTY, getBase64Texture(texture)));
        return profile;
    }

}
