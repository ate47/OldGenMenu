package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.inventory.container.RecipeBookContainer;
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
	// the items
	private List<ItemStorage> itemsVisible = new ArrayList<>();
	private List<ItemStorage> itemsInternal = new ArrayList<>();
	private int index = 0;
	public boolean selected = false;

	public CraftingInventorySystemTab(Function<Item, Boolean> validator) {
		this.validator = validator == null ? (t -> false) : validator;
	}

	public CraftingInventorySystemTab() {
		this(null);
	}

	public void addItem(ItemStorage item) {
		this.itemsInternal.add(item);
	}

	public boolean isFillable(Item item) {
		return this.validator.apply(item);
	}

	/**
	 * @return the size, between 3 and 1
	 */
	public int getViewSize() {
		return Math.min(3, itemsVisible.size());
	}

	public boolean showArrows() {
		return itemsVisible.size() != 1;
	}

	public int getSelectedIndex() {
		return index;
	}

	private ItemStorage get(int index) {
		return itemsVisible.get((index + itemsVisible.size()) % itemsVisible.size());
	}

	public void nextItem() {
		if (!itemsVisible.isEmpty()) {
			index = (index - 1 + itemsVisible.size()) % itemsVisible.size();
		}
	}

	public void lastItem() {
		if (!itemsVisible.isEmpty()) {
			index = (index + 1) % itemsVisible.size();
		}
	}

	/**
	 * @return the selected
	 */
	public ItemStorage getSelected() {
		if (index < 0 || index >= itemsVisible.size())
			index = 0;
		return itemsVisible.isEmpty() ? null : get(index);
	}

	public boolean loadSize(int width, int height) {
		index = 0;
		itemsVisible.clear();
		itemsInternal.stream().filter(i -> i.getRecipe().canCraftInDimensions(width, height))
				.forEach(itemsVisible::add);
		itemsVisible.sort(ItemStorage::compareTo);
		return !itemsVisible.isEmpty();
	}

	public ItemStorage render(int x, int y, RecipeBookContainer<?> inv, int mouseX, int mouseY) {
		int size = getViewSize();
		ItemStorage outStorage = null;
		if (size == 0)
			return outStorage; // at least one element
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
					storages[i].renderStack(inv, x + 2, top + i * 16 + 2, 12);
					if (RenderUtils.isIn(mouseX, mouseY, x + 2, top + i * 16 + 2, 12, 12))
						outStorage = storages[i];
				}
			} else {
				ItemStorage storage = getSelected();
				storage.renderStack(inv, x + 2, y + 2, 12);
				if (RenderUtils.isIn(mouseX, mouseY, x + 2, y + 2, 12, 12))
					outStorage = storage;
			}
			RenderUtils.renderText(MULTI_SLOT_SELECTED, x - 4, y - 1, 24, 18);
		} else {
			ItemStorage storage = getSelected();
			storage.renderStack(inv, x + 2, y + 2, 12);
			if (RenderUtils.isIn(mouseX, mouseY, x + 2, y + 2, 12, 12))
				outStorage = storage;
		}
		return outStorage;
	}
}
