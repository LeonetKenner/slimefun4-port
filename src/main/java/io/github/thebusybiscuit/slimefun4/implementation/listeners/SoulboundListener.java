package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.events.AsyncProfileLoadEvent;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.attributes.Soulbound;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

/**
 * This {@link Listener} is responsible for handling any {@link Soulbound} items.
 * A {@link Soulbound} {@link ItemStack} will not drop upon a {@link Player Player's} death.
 * Instead the {@link ItemStack} is saved and given back to the {@link Player} when they respawn.
 * <p>
 * The items are also stored within the {@link PlayerProfile} so they are not lost if the server
 * is restarted (or the {@link Player} disconnects) while still on the death screen.
 * 
 * @author TheBusyBiscuit
 *
 */
public class SoulboundListener implements Listener {

    /**
     * The Soulbound items of {@link Player Players} which are still alive on this server instance.
     * This is the primary data source for a regular death and respawn, as the {@link PlayerProfile}
     * may still be loading asynchronously when a {@link Player} dies.
     */
    private final Map<UUID, Map<Integer, ItemStack>> soulbound = new HashMap<>();

    public SoulboundListener(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDamage(PlayerDeathEvent e) {
        Map<Integer, ItemStack> items = new HashMap<>();
        Player p = e.getEntity();

        for (int slot = 0; slot < p.getInventory().getSize(); slot++) {
            ItemStack item = p.getInventory().getItem(slot);

            // Store soulbound items for later retrieval
            if (SlimefunUtils.isSoulbound(item, p.getWorld())) {
                items.put(slot, item);
            }
        }

        // There shouldn't even be any items in there, but let's be extra safe!
        Map<Integer, ItemStack> existingItems = soulbound.get(p.getUniqueId());

        if (existingItems == null) {
            soulbound.put(p.getUniqueId(), items);
        } else {
            existingItems.putAll(items);
        }

        // Persist the items so they survive a server restart while on the death screen (#3774)
        if (!items.isEmpty()) {
            PlayerProfile.find(p).ifPresent(profile -> profile.addSoulboundItems(items));
        }

        // Remove soulbound items from our drops
        e.getDrops().removeIf(itemStack -> SlimefunUtils.isSoulbound(itemStack, p.getWorld()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Map<Integer, ItemStack> items = soulbound.remove(p.getUniqueId());

        // The respawn event is synchronous, so the profile may not be loaded in time. We use the
        // in-memory items (if any) and simply make sure the persisted copy is cleared as well.
        PlayerProfile.find(p).ifPresent(PlayerProfile::clearSoulboundItems);

        if (items != null && !items.isEmpty()) {
            placeItems(p, items);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerProfile.find(p).ifPresent(profile -> returnSoulboundItems(p, profile));
    }

    /**
     * The {@link PlayerProfile} may still be loading when the {@link Player} joins.
     * If that is the case, the {@link PlayerJoinEvent} cannot return the items, so we
     * do it as soon as the profile has finished loading.
     */
    @EventHandler
    public void onProfileLoad(AsyncProfileLoadEvent e) {
        PlayerProfile profile = e.getProfile();
        Player p = profile.getPlayer();

        // We only want to return items to a living Player to avoid handing items back while on the death screen
        if (p != null && !p.isDead()) {
            Slimefun.runSync(() -> returnSoulboundItems(p, profile));
        }
    }

    private void returnSoulboundItems(@Nonnull Player p, @Nonnull PlayerProfile profile) {
        // The in-memory items take priority, they represent the most recent death
        Map<Integer, ItemStack> items = soulbound.remove(p.getUniqueId());

        if (items == null || items.isEmpty()) {
            items = new HashMap<>(profile.getSoulboundItems());
        }

        // Always clear the persisted items to avoid returning them twice
        profile.clearSoulboundItems();

        if (!items.isEmpty()) {
            placeItems(p, items);
        }
    }

    /**
     * This places the given items back into the {@link Player Player's} inventory.
     * If a slot is already occupied (or invalid), the item is put into the first free slot.
     * Any item that does not fit is dropped at the {@link Player Player's} {@link org.bukkit.Location}.
     *
     * @param p
     *            The {@link Player} to give the items to
     * @param items
     *            The items to give, indexed by their original inventory slot
     */
    private void placeItems(@Nonnull Player p, @Nonnull Map<Integer, ItemStack> items) {
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack item = entry.getValue();
            int slot = entry.getKey();
            boolean validSlot = slot >= 0 && slot < p.getInventory().getSize();
            ItemStack current = validSlot ? p.getInventory().getItem(slot) : null;

            if (validSlot && (current == null || current.getType() == Material.AIR)) {
                p.getInventory().setItem(slot, item);
            } else {
                // The original slot is occupied (or invalid), so try to find another free slot before dropping it
                Map<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
                leftovers.values().forEach(leftover -> p.getWorld().dropItemNaturally(p.getLocation(), leftover));
            }
        }
    }
}
