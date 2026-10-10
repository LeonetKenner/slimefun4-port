package io.github.thebusybiscuit.slimefun4.implementation.tasks.armor;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.items.armor.RainbowArmorPiece;

/**
 * The {@link RainbowArmorTask} is responsible for handling the change in color of any Rainbow Armor piece.
 *
 * @author martinbrom
 */
public class RainbowArmorTask extends AbstractArmorTask {

    private long currentColorIndex = 0;

    @Override
    protected void onTick() {
        currentColorIndex++;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onPlayerTick(Player p, PlayerProfile profile) {
        ItemStack[] armor = p.getInventory().getArmorContents();

        for (int i = 0; i < 4; i++) {
            ItemStack item = armor[i];

            if (item != null && item.hasItemMeta()) {
                /*
                 * We resolve the SlimefunItem from the actual ItemStack instead of relying on the
                 * PlayerProfile's cached armor piece. That cache is updated by a separate task and may
                 * still reference a previously worn rainbow piece, which caused normal or custom
                 * leather armor to be recolored after a quick armor swap (see issue #3931).
                 */
                SlimefunItem sfItem = SlimefunItem.getByItem(item);

                if (sfItem instanceof RainbowArmorPiece rainbowArmorPiece && rainbowArmorPiece.canUse(p, true)) {
                    updateRainbowArmor(item, rainbowArmorPiece);
                }
            }
        }
    }

    @ParametersAreNonnullByDefault
    private void updateRainbowArmor(ItemStack itemStack, RainbowArmorPiece armorPiece) {
        Color[] colors = armorPiece.getColors();
        Color newColor = colors[(int) (currentColorIndex % colors.length)];

        if (itemStack.getItemMeta() instanceof  LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(newColor);
            itemStack.setItemMeta(leatherArmorMeta);
        }
    }
}
