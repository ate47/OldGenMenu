package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CraftingGroup {
	private static int idCounter = 0;
	private static final List<CraftingGroup> GROUPS_INTERNAL = new ArrayList<>();
	public static final List<CraftingGroup> GROUPS = Collections.unmodifiableList(GROUPS_INTERNAL);

	public static final CraftingGroup STRUCTURES;
	public static final CraftingGroup TOOLS;
	public static final CraftingGroup FOOD;
	public static final CraftingGroup ARMORS;
	public static final CraftingGroup REDSTONE;
	public static final CraftingGroup TRANSPORT;
	public static final CraftingGroup MISCELLANEOUS;

	static {
		// order is important
		STRUCTURES = new CraftingGroup("oldgenmenu.menu.structures", new ItemStack(Blocks.OAK_PLANKS));
		TOOLS = new CraftingGroup("oldgenmenu.menu.tools", new ItemStack(Items.IRON_PICKAXE));
		FOOD = new CraftingGroup("oldgenmenu.menu.food", new ItemStack(Items.BREAD));
		ARMORS = new CraftingGroup("oldgenmenu.menu.armors", new ItemStack(Items.NETHERITE_CHESTPLATE));
		REDSTONE = new CraftingGroup("oldgenmenu.menu.redstone", new ItemStack(Items.REDSTONE));
		TRANSPORT = new CraftingGroup("oldgenmenu.menu.transport", new ItemStack(Blocks.RAIL));
		MISCELLANEOUS = new CraftingGroup("oldgenmenu.menu.miscellaneous", new ItemStack(Items.PAINTING));
	}

	private ITextComponent description;
	private ItemStack icon;
	public boolean selected = false;
	private int id;

	public CraftingGroup(String descriptionId, ItemStack icon) {
		this.id = ++idCounter;
		this.description = new TranslationTextComponent(descriptionId);
		this.icon = icon;
		GROUPS_INTERNAL.add(this);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public CraftingGroup(String description, Supplier<ItemStack> icon) {
		this(description, icon.get());
	}

	/**
	 * @return the icon
	 */
	public ItemStack getIcon() {
		return icon;
	}

	/**
	 * @return the description
	 */
	public ITextComponent getDescription() {
		return description;
	}
}
