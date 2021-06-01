package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

class CraftingInventorySystemTab {
	public static final ResourceLocation MULTI_BOTTOM = new ResourceLocation("textures/gui/oldgen/multi_bottom.png");
	public static final ResourceLocation MULTI_TOP = new ResourceLocation("textures/gui/oldgen/multi_top.png");
	public static final ResourceLocation MULTI_SLOT = new ResourceLocation("textures/gui/oldgen/multi_slot.png");
	public static final ResourceLocation MULTI_SLOT_SELECTED = new ResourceLocation(
			"textures/gui/oldgen/multi_slot_selected.png");
	private static final ItemStorage[] EMPTY_STORAGES = new ItemStorage[0];
	private Function<Item, Boolean> validator;
	// O(~1) to know if we know it
	private Set<Item> itemsMap = new HashSet<>();
	// the items
	private List<ItemStorage> items = new ArrayList<>();
	private int index = 0;
	public boolean selected = false;

	public CraftingInventorySystemTab(Function<Item, Boolean> validator) {
		this.validator = validator == null ? (t -> false) : validator;
	}

	public CraftingInventorySystemTab() {
		this(null);
	}

	public void addItem(ItemStorage item) {
		if (itemsMap.contains(item.getItemStack().getItem()))
			return;
		this.items.add(item);
		this.itemsMap.add(item.getItemStack().getItem());
	}

	public boolean isFillable(Item item) {
		return this.validator.apply(item) || itemsMap.contains(item);
	}

	/**
	 * @return the size, between 3 and 1
	 */
	public int getViewSize() {
		return Math.min(3, items.size());
	}

	public boolean showArrows() {
		return items.size() != 1;
	}

	private ItemStorage get(int index) {
		return items.get((index + items.size()) % items.size());
	}

	/**
	 * @return the selected
	 */
	public ItemStorage getSelected() {
		return get(index);
	}

	public void render(int x, int y) {
		int size = getViewSize();
		if (size == 0)
			return; // at least one element
		if (selected) {
			int top;
			int bottom;
			ItemStorage[] storages;
			switch (size) {
				case 3:
					storages = new ItemStorage[] { get(index - 1), get(index), get(index + 1) };

					top = y - 16;
					bottom = y + 32;

					break;
				case 2:
					storages = new ItemStorage[] { get(index), get(index + 1) };

					top = y;
					bottom = y + 32;

					break;
				default: // 1
					top = bottom = 0;
					storages = EMPTY_STORAGES;
					break;
			}

			if (showArrows()) { // show arrows
				RenderUtils.renderText(MULTI_TOP, x - 4, top - 8, 24, 8);
				RenderUtils.renderText(MULTI_BOTTOM, x - 4, bottom, 24, 8);
				for (int i = 0; i < storages.length; i++) {
					RenderUtils.renderText(MULTI_SLOT, x - 4, top + i * 16, 24, 16);
					RenderUtils.renderItem(storages[i].getItemStack(), x + 1, top + i * 16 + 1, 14);
				}
			} else
				RenderUtils.renderItem(getSelected().getItemStack(), x + 1, y + 1, 14);
			RenderUtils.renderText(MULTI_SLOT_SELECTED, x - 4, y - 1, 24, 18);
		} else {
			RenderUtils.renderItem(getSelected().getItemStack(), x + 1, y + 1, 14);
		}
	}
}
