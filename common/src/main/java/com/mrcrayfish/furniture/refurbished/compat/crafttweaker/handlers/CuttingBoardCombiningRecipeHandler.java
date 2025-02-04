package com.mrcrayfish.furniture.refurbished.compat.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.util.StringUtil;
import com.mrcrayfish.furniture.refurbished.compat.crafttweaker.Plugin;
import com.mrcrayfish.furniture.refurbished.crafting.CuttingBoardCombiningRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@IRecipeHandler.For(CuttingBoardCombiningRecipe.class)
public class CuttingBoardCombiningRecipeHandler implements IRecipeHandler<CuttingBoardCombiningRecipe>
{
    @Override
    public String dumpToCommandString(IRecipeManager<? super CuttingBoardCombiningRecipe> manager, RegistryAccess registryAccess, RecipeHolder<CuttingBoardCombiningRecipe> holder)
    {
        return String.format("%s.addRecipe(%s, %s, %s);",
            manager.getCommandString(),
            StringUtil.quoteAndEscape(holder.id()),
            IItemStack.ofMutable(holder.value().getResult()).getCommandString(),
            "[" + String.join(", ", holder.value().getIngredients().stream().map(ingredient -> IIngredient.fromIngredient(ingredient).getCommandString()).toArray(String[]::new)) + "]");
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super CuttingBoardCombiningRecipe> manager, CuttingBoardCombiningRecipe firstRecipe, U secondRecipe)
    {
        if(!(secondRecipe instanceof CuttingBoardCombiningRecipe))
            return false;
        NonNullList<Ingredient> firstIngredients = firstRecipe.getIngredients();
        NonNullList<Ingredient> secondIngredients = secondRecipe.getIngredients();
        int minSize = Math.min(firstIngredients.size(), secondIngredients.size());
        for(int i = 0; i < minSize; i++)
        {
            if(Collections.disjoint(firstIngredients.get(i).getStackingIds(), secondIngredients.get(i).getStackingIds()))
            {
                // TODO test
                Plugin.LOGGER.error("Cutting Board combining inputs cannot be empty");
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super CuttingBoardCombiningRecipe> manager, RegistryAccess registryAccess, CuttingBoardCombiningRecipe recipe)
    {
        IDecomposedRecipe decomposedRecipe = IDecomposedRecipe.builder()
            .with(BuiltinRecipeComponents.Input.INGREDIENTS, recipe.getIngredients().stream().map(IIngredient::fromIngredient).collect(Collectors.toList()))
            .with(BuiltinRecipeComponents.Output.ITEMS, IItemStack.ofMutable(recipe.getResult()))
            .build();
        return Optional.of(decomposedRecipe);
    }

    @Override
    public Optional<CuttingBoardCombiningRecipe> recompose(IRecipeManager<? super CuttingBoardCombiningRecipe> manager, RegistryAccess registryAccess, IDecomposedRecipe recipe)
    {
        Ingredient[] ingredients = recipe.getOrThrow(BuiltinRecipeComponents.Input.INGREDIENTS).stream().map(IIngredient::asVanillaIngredient).toArray(Ingredient[]::new);
        ItemStack result = recipe.getOrThrowSingle(BuiltinRecipeComponents.Output.ITEMS).getInternal();
        return Optional.of(new CuttingBoardCombiningRecipe(NonNullList.of(Ingredient.EMPTY, ingredients), result));
    }
}
