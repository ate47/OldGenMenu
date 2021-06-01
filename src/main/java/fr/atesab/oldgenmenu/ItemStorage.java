package fr.atesab.oldgenmenu;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStorage {
	private ItemStack is;

	public ItemStorage(Item it) {
		is = new ItemStack(it);
	}

	/**
	 * @return the is
	 */
	public ItemStack getItemStack() {
		return is;
	}
}
