package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CraftingInventorySystemGroup {
	private static final int PAGE_SIZE = 13;
	private List<CraftingInventorySystemTab> tabsInternal = new ArrayList<>();
	private List<CraftingInventorySystemTab> tabsUsable = new ArrayList<>();
	private CraftingGroup group;
	private int selected = 0;

	public CraftingInventorySystemGroup(CraftingGroup group) {
		this.group = group;
	}

	/**
	 * @return the tabs (unmodifiable)
	 */
	public List<CraftingInventorySystemTab> getVisibleTabs() {
		int pageStart = selected - (selected % PAGE_SIZE);
		return Collections.unmodifiableList(tabsUsable.subList(Math.min(tabsUsable.size(), pageStart),
				Math.min(tabsUsable.size(), pageStart + PAGE_SIZE)));
	}

	/**
	 * @return the tabsInternal (unmodifiable)
	 */
	public List<CraftingInventorySystemTab> getInternalTabs() {
		return Collections.unmodifiableList(tabsInternal);
	}

	/**
	 * @return the selected tab or null if empty
	 */
	public CraftingInventorySystemTab getSelected() {
		if (tabsUsable.isEmpty())
			return null;
		if (selected < 0 || selected >= tabsUsable.size()) {
			selected = 0;
		}
		CraftingInventorySystemTab tab = tabsUsable.get(selected);
		tab.selected = true;
		return tab;
	}

	/**
	 * Load the visible tab with the matrix size
	 * 
	 * @param width  matrix width
	 * @param height matrix height
	 */
	public void loadSize(int width, int height) {
		CraftingInventorySystemTab t;
		t = getSelected();
		if (t != null)
			t.selected = false;
		tabsUsable.clear();
		tabsInternal.stream().filter(g -> g.loadSize(width, height)).forEach(tabsUsable::add);
		t = getSelected();
		if (t != null)
			t.selected = true;
	}

	/**
	 * add a tab, a {@link #loadSize(int, int)} can be required if added while a
	 * menu is shown
	 * 
	 * @param tab the tab to add
	 */
	public void add(CraftingInventorySystemTab tab) {
		tabsInternal.add(tab);
	}

	public void selectVisible(int index) {
		int future = selected - (selected % PAGE_SIZE) + index;
		if (future >= tabsUsable.size())
			return;

		getSelected().selected = false;
		selected = future;
		getSelected().selected = true;
	}

	/**
	 * Next tab
	 */
	public void nextItem() {
		if (!tabsInternal.isEmpty()) {
			getSelected().selected = false;
			selected = (selected + 1) % tabsUsable.size();
			getSelected().selected = true;
		}
	}

	/**
	 * Last tab
	 */
	public void lastItem() {
		if (!tabsInternal.isEmpty()) {
			getSelected().selected = false;
			selected = (selected - 1 + tabsUsable.size()) % tabsUsable.size();
			getSelected().selected = true;
		}
	}

	private int mod(int a, int b) {
		while (a < 0)
			a += b;
		return a % b;
	}

	public int getSelectedIndex() {
		return selected;
	}

	/**
	 * Next tab
	 */
	public void nextPage() {
		if (!tabsInternal.isEmpty() && showPageArrows()) {
			getSelected().selected = false;
			int page = mod(selected / PAGE_SIZE + 1,
					tabsUsable.size() / PAGE_SIZE + (tabsUsable.size() % PAGE_SIZE == 0 ? 0 : 1));
			int delta = selected % PAGE_SIZE;
			selected = Math.min(page * PAGE_SIZE + delta, tabsUsable.size() - 1);
			getSelected().selected = true;
		}
	}

	/**
	 * Last tab
	 */
	public void lastPage() {
		if (!tabsInternal.isEmpty() && showPageArrows()) {
			getSelected().selected = false;
			int page = mod(selected / PAGE_SIZE - 1,
					tabsUsable.size() / PAGE_SIZE + (tabsUsable.size() % PAGE_SIZE == 0 ? 0 : 1));
			int delta = selected % PAGE_SIZE;
			selected = Math.min(page * PAGE_SIZE + delta, tabsUsable.size() - 1);
			getSelected().selected = true;
		}
	}

	public boolean showPageArrows() {
		return tabsUsable.size() > PAGE_SIZE;
	}

	/**
	 * @return the group
	 */
	public CraftingGroup getGroup() {
		return group;
	}

}
