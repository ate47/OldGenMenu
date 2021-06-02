package fr.atesab.oldgenmenu;

import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class ItemStorage  implements Comparable<ItemStorage> {
	private IRecipe recipe;

	public ItemStorage(IRecipe recipe) {
		this.recipe = recipe;
	}

	/**
	 * @return the is
	 */
	public ItemStack getItemStack() {
		return recipe.getResultItem();
	}

	/**
	 * @return the recipe
	 */
	public IRecipe getRecipe() {
		return recipe;
	}

	public void renderStack(RecipeBookContainer<?> inv, int x, int y, int size) {
		RenderUtils.renderItem(getItemStack(), x, y, size);
		if (!inv.recipeMatches(recipe))
			RenderUtils.renderFill(x, y, size, size, 0x44dddddd);
	}

	@Override
	public int compareTo(ItemStorage o) {
		int id = Item.getId(getItemStack().getItem());
		return id - Item.getId(o.getItemStack().getItem());
	}
}
