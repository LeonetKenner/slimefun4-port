package io.github.thebusybiscuit.slimefun4.implementation.items.autocrafters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

import io.github.thebusybiscuit.slimefun4.implementation.tasks.AsyncRecipeChoiceTask;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

/**
 * The {@link SpecialRecipe} is an {@link AbstractRecipe} implementation for vanilla
 * "special" crafting recipes which are not exposed as a {@link org.bukkit.inventory.Recipe}
 * by Bukkit/Spigot (e.g. firework rockets or shulker box coloring).
 * <p>
 * Since the {@link org.bukkit.Server} does not provide these recipes through the Bukkit
 * API, we create a pseudo-recipe with fixed ingredients and a fixed result.
 * 
 * @author TheBusyBiscuit
 * 
 * @see VanillaAutoCrafter
 * @see SpecialRecipes
 *
 */
class SpecialRecipe extends AbstractRecipe {

    private final int[] slots = { 11, 12, 13, 20, 21, 22, 29, 30, 31 };
    private final NamespacedKey key;
    private final RecipeChoice[] choices;

    SpecialRecipe(@Nonnull NamespacedKey key, @Nonnull RecipeChoice[] choices, @Nonnull ItemStack result) {
        super(toPredicates(choices), result);

        this.key = key;
        this.choices = choices;
    }

    @Nonnull
    private static Collection<Predicate<ItemStack>> toPredicates(@Nonnull RecipeChoice[] choices) {
        Preconditions.checkNotNull(choices, "The choices cannot be null!");
        Preconditions.checkArgument(choices.length > 0, "The choices cannot be empty!");

        List<Predicate<ItemStack>> predicates = new ArrayList<>(choices.length);

        for (RecipeChoice choice : choices) {
            predicates.add(choice);
        }

        return predicates;
    }

    /**
     * This returns a copy of this {@link SpecialRecipe}.
     * <p>
     * The recipes are stored as immutable templates, this method makes sure that the
     * enabled-state of one {@link VanillaAutoCrafter} does not affect any other one.
     *
     * @return A copy of this {@link SpecialRecipe}
     */
    @Nonnull
    SpecialRecipe copy() {
        RecipeChoice[] copies = new RecipeChoice[choices.length];

        for (int i = 0; i < choices.length; i++) {
            copies[i] = choices[i].clone();
        }

        return new SpecialRecipe(key, copies, getResult().clone());
    }

    @Override
    public void show(@Nonnull ChestMenu menu, @Nonnull AsyncRecipeChoiceTask task) {
        Preconditions.checkNotNull(menu, "The ChestMenu cannot be null!");
        Preconditions.checkNotNull(task, "The RecipeChoiceTask cannot be null!");

        menu.replaceExistingItem(24, getResult().clone());
        menu.addMenuClickHandler(24, ChestMenuUtils.getEmptyClickHandler());

        for (int i = 0; i < 9; i++) {
            ItemStack display = null;

            if (i < choices.length) {
                RecipeChoice choice = choices[i];
                display = choice.getItemStack().clone();

                if (choice instanceof MaterialChoice materialChoice && materialChoice.getChoices().size() > 1) {
                    task.add(slots[i], materialChoice);
                }
            }

            menu.replaceExistingItem(slots[i], display);
            menu.addMenuClickHandler(slots[i], ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
