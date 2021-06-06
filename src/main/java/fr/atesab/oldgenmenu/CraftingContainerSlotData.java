package fr.atesab.oldgenmenu;

import java.util.Objects;

public class CraftingContainerSlotData {
	public static final CraftingContainerSlotData PLAYER_INVENTORY_DATA = new CraftingContainerSlotData(
			CraftingSlotData.ofInterval(9, 35), CraftingSlotData.ofInterval(1, 4), 0,
			CraftingSlotData.ofInterval(39, 66));
	public static final CraftingContainerSlotData CREATIVE_INVENTORY_DATA = new CraftingContainerSlotData(
			CraftingSlotData.ofInterval(9, 36), CraftingSlotData.ofInterval(1, 9), 0,
			CraftingSlotData.ofInterval(36, 45));

	@FunctionalInterface
	interface CraftingSlotData {
		static CraftingSlotData ofInterval(int min, int max) {
			return index -> (index >= min && index <= max) ? index - min : -1;
		}

		int findIndex(int index);
	}

	public final CraftingSlotData inventoryData;
	public final CraftingSlotData craftingData;
	public final CraftingSlotData barData;

	public final int outputSlot;

	public CraftingContainerSlotData(CraftingSlotData inventoryData, CraftingSlotData craftingData, int outputSlot,
			CraftingSlotData barData) {
		this.inventoryData = Objects.requireNonNull(inventoryData, "inventoryData can't be null");
		this.craftingData = Objects.requireNonNull(craftingData, "craftingData can't be null");
		this.barData = Objects.requireNonNull(barData, "barData can't be null");
		this.outputSlot = outputSlot;
	}

}
