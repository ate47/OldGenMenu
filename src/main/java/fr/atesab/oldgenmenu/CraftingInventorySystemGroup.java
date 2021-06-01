package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.List;

public class CraftingInventorySystemGroup {
	private List<CraftingInventorySystemTab> tabs = new ArrayList<>();
	private CraftingGroup group;
	private int selected = 0;

	public CraftingInventorySystemGroup(CraftingGroup group) {
		this.group = group;
	}

	/**
	 * @return the tabs
	 */
	public List<CraftingInventorySystemTab> getTabs() {
		return tabs;
	}

	/**
	 * @return the selected
	 */
	public CraftingInventorySystemTab getSelected() {
		if (selected < 0 && selected >= tabs.size()) {
			selected = 0;
		}
		if (tabs.isEmpty())
			return null;
		CraftingInventorySystemTab tab = tabs.get(selected);
		tab.selected = true;
		return tab;
	}

	public void nextItem() {
		if (!tabs.isEmpty()) {
			getSelected().selected = false;
			selected = (selected + 1) % tabs.size();
			getSelected().selected = true;
		}
	}

	public void lastItem() {
		if (!tabs.isEmpty()) {
			getSelected().selected = false;
			selected = (selected - 1 + tabs.size()) % tabs.size();
			getSelected().selected = true;
		}
	}

	/**
	 * @return the group
	 */
	public CraftingGroup getGroup() {
		return group;
	}

}
