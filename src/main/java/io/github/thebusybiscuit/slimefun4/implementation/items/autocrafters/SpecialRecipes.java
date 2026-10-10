package io.github.thebusybiscuit.slimefun4.implementation.items.autocrafters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * This holds all {@link SpecialRecipe SpecialRecipes} which allow the {@link VanillaAutoCrafter}
 * to craft items that only have a vanilla "special" recipe.
 * <p>
 * These recipes cannot be obtained through the Bukkit API, so we define them manually here.
 * 
 * @author TheBusyBiscuit
 * 
 * @see SpecialRecipe
 * @see VanillaAutoCrafter
 *
 */
final class SpecialRecipes {

    private static final String NAMESPACE = "slimefun";

    private static List<SpecialRecipe> recipes;

    private SpecialRecipes() {}

    /**
     * This returns all registered {@link SpecialRecipe SpecialRecipes}.
     * <p>
     * The returned {@link List} should be treated as read-only.
     *
     * @return All {@link SpecialRecipe SpecialRecipes}
     */
    @Nonnull
    static synchronized List<SpecialRecipe> get() {
        if (recipes == null) {
            recipes = createRecipes();
        }

        return recipes;
    }

    /**
     * This returns a fresh copy of the {@link SpecialRecipe} with the given {@link NamespacedKey},
     * or null if no such recipe exists.
     * <p>
     * A copy is returned so that the enabled-state of one {@link VanillaAutoCrafter} does not
     * affect any other one.
     *
     * @param key
     *            The key to look up, as returned by {@link SpecialRecipe#toString()}
     *
     * @return A copy of the matching {@link SpecialRecipe} or null
     */
    @Nullable
    static SpecialRecipe getByKey(@Nullable String key) {
        if (key == null) {
            return null;
        }

        for (SpecialRecipe recipe : get()) {
            if (recipe.toString().equals(key)) {
                return recipe.copy();
            }
        }

        return null;
    }

    @Nonnull
    private static List<SpecialRecipe> createRecipes() {
        List<SpecialRecipe> list = new ArrayList<>();

        createFireworkRockets(list);
        createFireworkStars(list);
        createShulkerBoxColoring(list);

        return list;
    }

    /**
     * A firework rocket is crafted using paper and one to three gunpowder,
     * the amount of gunpowder determining the flight duration.
     */
    private static void createFireworkRockets(@Nonnull List<SpecialRecipe> list) {
        for (int power = 1; power <= 3; power++) {
            RecipeChoice[] choices = new RecipeChoice[power + 1];
            choices[0] = new MaterialChoice(Material.PAPER);

            for (int i = 1; i < choices.length; i++) {
                choices[i] = new MaterialChoice(Material.GUNPOWDER);
            }

            ItemStack result = new ItemStack(Material.FIREWORK_ROCKET);
            FireworkMeta meta = (FireworkMeta) result.getItemMeta();
            meta.setPower(power);
            result.setItemMeta(meta);

            list.add(new SpecialRecipe(key("auto_crafter_firework_rocket_" + power), choices, result));
        }
    }

    /**
     * A firework star is crafted using gunpowder and a single dye.
     * We only support the simplest form (a single colour, small ball),
     * which is a reasonable approximation.
     */
    private static void createFireworkStars(@Nonnull List<SpecialRecipe> list) {
        for (DyeColor color : DyeColor.values()) {
            Material dye = Material.matchMaterial(color.name() + "_DYE");

            if (dye == null) {
                continue;
            }

            ItemStack result = new ItemStack(Material.FIREWORK_STAR);
            FireworkEffectMeta meta = (FireworkEffectMeta) result.getItemMeta();
            meta.setEffect(FireworkEffect.builder().withColor(color.getColor()).build());
            result.setItemMeta(meta);

            RecipeChoice[] choices = { new MaterialChoice(Material.GUNPOWDER), new MaterialChoice(dye) };
            list.add(new SpecialRecipe(key("auto_crafter_firework_star_" + color.name().toLowerCase(Locale.ROOT)), choices, result));
        }
    }

    /**
     * A shulker box is coloured by combining a plain shulker box with a single dye.
     */
    private static void createShulkerBoxColoring(@Nonnull List<SpecialRecipe> list) {
        for (DyeColor color : DyeColor.values()) {
            Material dye = Material.matchMaterial(color.name() + "_DYE");
            Material shulkerBox = Material.matchMaterial(color.name() + "_SHULKER_BOX");

            if (dye == null || shulkerBox == null) {
                continue;
            }

            RecipeChoice[] choices = { new MaterialChoice(Material.SHULKER_BOX), new MaterialChoice(dye) };
            list.add(new SpecialRecipe(key("auto_crafter_shulker_box_" + color.name().toLowerCase(Locale.ROOT)), choices, new ItemStack(shulkerBox)));
        }
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    private static NamespacedKey key(@Nonnull String name) {
        return new NamespacedKey(NAMESPACE, name);
    }
}
