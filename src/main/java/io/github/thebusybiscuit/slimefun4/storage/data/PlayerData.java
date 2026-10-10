package io.github.thebusybiscuit.slimefun4.storage.data;

import com.google.common.annotations.Beta;

import io.github.thebusybiscuit.slimefun4.api.gps.Waypoint;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

/**
 * The data which backs {@link io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile}
 *
 * <b>This API is still experimental, it may change without notice.</b>
 */
// TODO: Should we keep this in PlayerProfile?
@Beta
public class PlayerData {

    private final Set<Research> researches = new HashSet<>();
    private final Map<Integer, PlayerBackpack> backpacks = new HashMap<>();
    private final Set<Waypoint> waypoints = new HashSet<>();

    /**
     * This holds any Soulbound items which have not yet been returned to the
     * {@link org.bukkit.entity.Player}. The key is the inventory slot the item
     * was in when the {@link org.bukkit.entity.Player} died.
     * <p>
     * They are persisted so that they are not lost when the server is restarted
     * while a {@link org.bukkit.entity.Player} is still on the death screen.
     *
     * @see io.github.thebusybiscuit.slimefun4.implementation.listeners.SoulboundListener
     */
    private final Map<Integer, ItemStack> soulboundItems = new HashMap<>();

    public PlayerData(Set<Research> researches, Map<Integer, PlayerBackpack> backpacks, Set<Waypoint> waypoints) {
        this(researches, backpacks, waypoints, new HashMap<>());
    }

    public PlayerData(Set<Research> researches, Map<Integer, PlayerBackpack> backpacks, Set<Waypoint> waypoints, Map<Integer, ItemStack> soulboundItems) {
        this.researches.addAll(researches);
        this.backpacks.putAll(backpacks);
        this.waypoints.addAll(waypoints);
        this.soulboundItems.putAll(soulboundItems);
    }

    public Set<Research> getResearches() {
        return researches;
    }

    public void addResearch(@Nonnull Research research) {
        Validate.notNull(research, "Cannot add a 'null' research!");
        researches.add(research);
    }

    public void removeResearch(@Nonnull Research research) {
        Validate.notNull(research, "Cannot remove a 'null' research!");
        researches.remove(research);
    }

    @Nonnull
    public Map<Integer, PlayerBackpack> getBackpacks() {
        return backpacks;
    }

    @Nonnull
    public PlayerBackpack getBackpack(int id) {
        return backpacks.get(id);
    }

    public void addBackpack(@Nonnull PlayerBackpack backpack) {
        Validate.notNull(backpack, "Cannot add a 'null' backpack!");
        backpacks.put(backpack.getId(), backpack);
    }

    public void removeBackpack(@Nonnull PlayerBackpack backpack) {
        Validate.notNull(backpack, "Cannot remove a 'null' backpack!");
        backpacks.remove(backpack.getId());
    }

    public Set<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(@Nonnull Waypoint waypoint) {
        Validate.notNull(waypoint, "Cannot add a 'null' waypoint!");

        for (Waypoint wp : waypoints) {
            if (wp.getId().equals(waypoint.getId())) {
                throw new IllegalArgumentException("A Waypoint with that id already exists for this Player");
            }
        }

        // Limited to 21 due to limited UI space and no pagination
        if (waypoints.size() >= 21) {
            return; // not sure why this doesn't throw but the one above does...
        }

        waypoints.add(waypoint);
    }

    public void removeWaypoint(@Nonnull Waypoint waypoint) {
        Validate.notNull(waypoint, "Cannot remove a 'null' waypoint!");
        waypoints.remove(waypoint);
    }

    /**
     * This returns a mutable {@link Map} of all Soulbound {@link ItemStack ItemStacks}
     * which still have to be returned to the {@link org.bukkit.entity.Player}.
     * The key is the inventory slot the item was in.
     *
     * @return A {@link Map} of the not yet returned Soulbound items
     */
    @Nonnull
    public Map<Integer, ItemStack> getSoulboundItems() {
        return soulboundItems;
    }
}
