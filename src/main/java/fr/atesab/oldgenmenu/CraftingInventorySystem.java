package fr.atesab.oldgenmenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class CraftingInventorySystem {
	private static final Map<ItemGroup, CraftingGroup> itemgroupToGroup = new HashMap<>();
	static {
		itemgroupToGroup.put(ItemGroup.TAB_BUILDING_BLOCKS, CraftingGroup.STRUCTURES);
		itemgroupToGroup.put(ItemGroup.TAB_COMBAT, CraftingGroup.TOOLS);
		itemgroupToGroup.put(ItemGroup.TAB_TOOLS, CraftingGroup.TOOLS);
		itemgroupToGroup.put(ItemGroup.TAB_REDSTONE, CraftingGroup.REDSTONE);
		itemgroupToGroup.put(ItemGroup.TAB_FOOD, CraftingGroup.FOOD);
	}
	
	
	private static CraftingGroup tryFindByGroup(Item it) {
		ItemGroup category = it.getItemCategory();
		CraftingGroup group = category == null ? null : itemgroupToGroup.get(category);
		return group == null ? CraftingGroup.MISCELLANEOUS : group;
	}
	
	private boolean loaded = false;
	private final Map<Function<Item, Boolean>, CraftingGroup> classToGroup = new HashMap<>();
	private final Map<CraftingGroup, CraftingInventorySystemGroup> itemMap = new HashMap<>();
	private int selected;

	public CraftingInventorySystem() {
		// none
	}

	public CraftingGroup getGroup(Item it) {
		Class<?> c = it.getClass();
		for (Entry<Function<Item, Boolean>, CraftingGroup> e : classToGroup.entrySet())
			if (e.getKey().apply(it).booleanValue())
				return e.getValue();
		
		return tryFindByGroup(it);
	}
	private void createCustomTab(CraftingGroup g, Function<Item, Boolean> validator, Item... items) {
		List<CraftingInventorySystemTab> tabs = itemMap.computeIfAbsent(g, CraftingInventorySystemGroup::new).getTabs();
		CraftingInventorySystemTab tab = new CraftingInventorySystemTab(validator);
		if (validator != null)
			classToGroup.put(validator, g);
		for (Item it : items)
			tab.addItem(new ItemStorage(it));
		tabs.add(tab);
	}

	private void createCustomTabC(CraftingGroup g, Class<?> cls, Item... items) {
		createCustomTab(g, i -> cls.isInstance(i), items);
	}

	private CraftingInventorySystemTab findOrCreateTab(Item it, CraftingGroup g) {
		List<CraftingInventorySystemTab> tabs = itemMap.computeIfAbsent(g, CraftingInventorySystemGroup::new).getTabs();
		for (CraftingInventorySystemTab tab : tabs) {
			if (tab.isFillable(it))
				return tab;
		}
		CraftingInventorySystemTab tab = new CraftingInventorySystemTab();
		tabs.add(tab);
		return tab;
	}

	private void loadItem(Item it) {
		CraftingGroup g = tryFindByGroup(it);
		CraftingInventorySystemTab tab = findOrCreateTab(it, g);
		tab.addItem(new ItemStorage(it));
	}

	public Function<Item, Boolean> matchRegexBlock(String regex) {
		Pattern pattern = Pattern.compile(regex);
		return i -> {
			if (i instanceof BlockItem) {
				Block b = ((BlockItem) i).getBlock();
				return pattern.matcher(b.getRegistryName().toString()).matches();
			}
			return false;
		};
	}

	public Function<Item, Boolean> matchRegexItem(String regex) {
		Pattern pattern = Pattern.compile(regex);
		return i -> pattern.matcher(i.getRegistryName().toString()).matches();
	}

	public Function<Item, Boolean> matchArmorItem(EquipmentSlotType slot) {
		return i -> {
			if (!(i instanceof ArmorItem))
				return false;
			ArmorItem ai = (ArmorItem) i;
			return ai.getSlot() == slot;
		};
	}

	public void reloadSystem() {
		itemMap.clear();
		CraftingGroup.GROUPS.forEach(g -> {
			g.selected = false;
			itemMap.put(g, new CraftingInventorySystemGroup(g));
		});

		createCustomTab(CraftingGroup.STRUCTURES, matchRegexBlock(".*_planks"), Items.OAK_PLANKS);
		createCustomTab(CraftingGroup.STRUCTURES, null, Items.STICK, Items.LADDER);
		createCustomTab(CraftingGroup.STRUCTURES, null, Items.END_STONE_BRICKS, Items.QUARTZ_BLOCK, Items.QUARTZ_PILLAR,
				Items.QUARTZ_BRICKS, Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS,
				Items.CHISELED_STONE_BRICKS, Items.MOSSY_COBBLESTONE, Items.RED_NETHER_BRICKS, Items.DIORITE,
				Items.GRANITE, Items.ANDESITE, Items.POLISHED_ANDESITE, Items.POLISHED_DIORITE, Items.POLISHED_GRANITE,
				Items.PURPUR_BLOCK);
		createCustomTab(CraftingGroup.STRUCTURES, null, Items.SANDSTONE, Items.RED_SANDSTONE, Items.SMOOTH_SANDSTONE,
				Items.SMOOTH_RED_SANDSTONE, Items.SANDSTONE_WALL, Items.RED_SANDSTONE_WALL, Items.NETHER_BRICK,
				Items.COARSE_DIRT, Items.PRISMARINE_BRICKS, Items.PRISMARINE_WALL, Items.PRISMARINE_BRICKS,
				Items.SNOW_BLOCK, Items.SNOW, Items.CLAY, Items.BRICK, Items.BONE_BLOCK);
		createCustomTab(CraftingGroup.STRUCTURES, null, Items.CRAFTING_TABLE, Items.FURNACE, Items.ENCHANTING_TABLE,
				Items.ANVIL, Items.BREWING_STAND);
		createCustomTab(CraftingGroup.STRUCTURES, null, Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST,
				Items.SHULKER_BOX);

		createCustomTabC(CraftingGroup.STRUCTURES, BedItem.class, Items.WHITE_BED);

		createCustomTabC(CraftingGroup.TOOLS, SwordItem.class, Items.WOODEN_SWORD);
		createCustomTabC(CraftingGroup.TOOLS, PickaxeItem.class, Items.WOODEN_PICKAXE);
		createCustomTabC(CraftingGroup.TOOLS, ShovelItem.class, Items.WOODEN_SHOVEL);
		createCustomTabC(CraftingGroup.TOOLS, AxeItem.class, Items.WOODEN_AXE);
		createCustomTabC(CraftingGroup.TOOLS, HoeItem.class, Items.WOODEN_HOE);

		createCustomTab(CraftingGroup.TOOLS, null, Items.FLINT_AND_STEEL, Items.SHEARS);

		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.HEAD), Items.LEATHER_HELMET);
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.CHEST), Items.LEATHER_CHESTPLATE);
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.LEGS), Items.LEATHER_LEGGINGS);
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.FEET), Items.LEATHER_BOOTS);

		IForgeRegistry<Item> items = ForgeRegistries.ITEMS;
		IForgeRegistry<Block> blocks = ForgeRegistries.BLOCKS;
		selected = 0;
		getSelected().selected = true;
		// load items
		items.forEach(this::loadItem);
		// load blocks
		blocks.forEach(b -> loadItem(b.asItem()));

		loaded = true;
	}

	public CraftingGroup getSelected() {
		if (selected < 0 || selected >= CraftingGroup.GROUPS.size())
			selected = 0;
		return CraftingGroup.GROUPS.get(selected);
	}

	public void nextGroup() {
		getSelected().selected = false;
		selected = (selected + 1) % CraftingGroup.GROUPS.size();
		getSelected().selected = true;
	}

	public void lastGroup() {
		getSelected().selected = false;
		selected = (selected - 1 + CraftingGroup.GROUPS.size()) % CraftingGroup.GROUPS.size();
		getSelected().selected = true;
	}

	public CraftingInventorySystem loadIfRequired() {
		if (!loaded) {
			reloadSystem();
		}
		return this;
	}

	public void select(CraftingGroup group) {
		getSelected().selected = false;
		for (int i = 0; i < CraftingGroup.GROUPS.size(); i++) {
			CraftingGroup g = CraftingGroup.GROUPS.get(i);
			if (g.getId() == group.getId()) {
				selected = i;
				g.selected = true;
				return;
			}
		}
	}

	public CraftingInventorySystemGroup getSysGroup() {
		return itemMap.computeIfAbsent(getSelected(), CraftingInventorySystemGroup::new);
	}
}
