package fr.atesab.oldgenmenu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.OnAStickItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SaddleItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

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
	private final Map<ItemStack, IRecipe<?>> crafts = new HashMap<>();
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

	private void createCustomTab(CraftingGroup g, Function<Item, Boolean> validator) {
		CraftingInventorySystemGroup craftingInventorySystemGroup = itemMap.computeIfAbsent(g,
				CraftingInventorySystemGroup::new);
		CraftingInventorySystemTab tab = new CraftingInventorySystemTab(validator);
		if (validator != null)
			classToGroup.put(validator, g);
		craftingInventorySystemGroup.add(tab);
	}

	private void createCustomTabC(CraftingGroup g, Class<?> cls) {
		createCustomTab(g, i -> cls.isInstance(i));
	}

	private void createCustomTabB(CraftingGroup g, Class<?> cls) {
		createCustomTab(g, i -> i instanceof BlockItem && cls.isInstance(((BlockItem) i).getBlock()));
	}

	private void createCustomTabC(CraftingGroup g, Class<?> cls, Item... items) {
		Set<Item> iSet = new HashSet<>();
		iSet.addAll(Arrays.asList(items));
		if (cls == null)
			createCustomTab(g, iSet::contains);
		else
			createCustomTab(g, i -> iSet.contains(i) || cls.isInstance(i));
	}

	private void createCustomTabB(CraftingGroup g, Class<?> cls, Item... items) {
		Set<Item> iSet = new HashSet<>();
		iSet.addAll(Arrays.asList(items));
		if (cls == null)
			createCustomTab(g, i -> i instanceof BlockItem && cls.isInstance(((BlockItem) i).getBlock()));
		else
			createCustomTab(g,
					i -> iSet.contains(i) || i instanceof BlockItem && cls.isInstance(((BlockItem) i).getBlock()));
	}

	private CraftingInventorySystemTab findOrCreateTab(Item it, CraftingGroup g) {
		for (CraftingInventorySystemGroup tabs : itemMap.values())
			for (CraftingInventorySystemTab tab : tabs.getInternalTabs()) {
				if (tab.isFillable(it))
					return tab;
			}
		CraftingInventorySystemGroup tabs = itemMap.computeIfAbsent(g, CraftingInventorySystemGroup::new);
		CraftingInventorySystemTab tab = new CraftingInventorySystemTab();
		tabs.add(tab);
		return tab;
	}

	private void loadRecipe(IRecipe<?> recipe) {
		ItemStack stack = recipe.getResultItem();
		crafts.put(stack, recipe);
		Item it = stack.getItem();
		CraftingGroup g = tryFindByGroup(it);
		CraftingInventorySystemTab tab = findOrCreateTab(it, g);
		tab.addItem(new ItemStorage(recipe));
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
		crafts.clear();
		CraftingGroup.GROUPS.forEach(g -> {
			g.selected = false;
			itemMap.put(g, new CraftingInventorySystemGroup(g));
		});

		createCustomTab(CraftingGroup.STRUCTURES, matchRegexBlock(".*_planks"));
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.STICK, Items.LADDER);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.END_STONE_BRICKS, Items.QUARTZ_BLOCK,
				Items.QUARTZ_PILLAR, Items.QUARTZ_BRICKS, Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS,
				Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.MOSSY_COBBLESTONE,
				Items.RED_NETHER_BRICKS, Items.DIORITE, Items.GRANITE, Items.ANDESITE, Items.POLISHED_ANDESITE,
				Items.POLISHED_DIORITE, Items.POLISHED_GRANITE, Items.PURPUR_BLOCK);
		createCustomTabB(CraftingGroup.STRUCTURES, WallBlock.class, Items.SANDSTONE, Items.RED_SANDSTONE,
				Items.SMOOTH_SANDSTONE, Items.SMOOTH_RED_SANDSTONE, Items.NETHER_BRICK, Items.COARSE_DIRT,
				Items.PRISMARINE_BRICKS, Items.PRISMARINE_BRICKS, Items.SNOW_BLOCK, Items.SNOW, Items.CLAY, Items.BRICK,
				Items.BONE_BLOCK);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.CRAFTING_TABLE, Items.FURNACE, Items.ENCHANTING_TABLE,
				Items.ANVIL, Items.BREWING_STAND);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST,
				Items.SHULKER_BOX);
		createCustomTab(CraftingGroup.STRUCTURES, matchRegexBlock(".*_wood"));

		createCustomTabC(CraftingGroup.STRUCTURES, BedItem.class);
		createCustomTabB(CraftingGroup.STRUCTURES, StairsBlock.class);
		createCustomTabB(CraftingGroup.STRUCTURES, SlabBlock.class);
		createCustomTabB(CraftingGroup.STRUCTURES, AbstractGlassBlock.class);

		createCustomTabC(CraftingGroup.TOOLS, SwordItem.class);
		createCustomTabC(CraftingGroup.TOOLS, PickaxeItem.class);
		createCustomTabC(CraftingGroup.TOOLS, ShovelItem.class);
		createCustomTabC(CraftingGroup.TOOLS, AxeItem.class);
		createCustomTabC(CraftingGroup.TOOLS, HoeItem.class);

		createCustomTabC(CraftingGroup.TOOLS, null, Items.FLINT_AND_STEEL, Items.SHEARS);
		createCustomTabC(CraftingGroup.TOOLS, ShootableItem.class);
		createCustomTabC(CraftingGroup.TOOLS, ArrowItem.class);
		createCustomTabC(CraftingGroup.TOOLS, null, Items.COMPASS, Items.CLOCK);
		createCustomTabC(CraftingGroup.TOOLS, ShieldItem.class);
		createCustomTabC(CraftingGroup.TOOLS, FishingRodItem.class);

		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.HEAD));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.CHEST));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.LEGS));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.FEET));

		createCustomTabB(CraftingGroup.TRANSPORT, AbstractRailBlock.class);
		createCustomTabC(CraftingGroup.TRANSPORT, MinecartItem.class, Items.SADDLE, Items.ELYTRA);
		createCustomTabC(CraftingGroup.TRANSPORT, ElytraItem.class);
		createCustomTabC(CraftingGroup.TRANSPORT, SaddleItem.class);
		createCustomTabC(CraftingGroup.TRANSPORT, BoatItem.class);
		createCustomTabC(CraftingGroup.TRANSPORT, OnAStickItem.class);

		createCustomTabB(CraftingGroup.REDSTONE, RedstoneBlock.class, Items.REDSTONE);
		createCustomTabC(CraftingGroup.REDSTONE, null, Items.REPEATER, Items.COMPARATOR, Items.DAYLIGHT_DETECTOR,
				Items.TRIPWIRE_HOOK, Items.LEVER, Items.REDSTONE_TORCH);
		createCustomTabB(CraftingGroup.REDSTONE, AbstractPressurePlateBlock.class);
		createCustomTabB(CraftingGroup.REDSTONE, PistonBlock.class);
		createCustomTabB(CraftingGroup.REDSTONE, TrapDoorBlock.class);
		createCustomTabB(CraftingGroup.REDSTONE, AbstractButtonBlock.class);
		createCustomTabB(CraftingGroup.REDSTONE, FenceGateBlock.class);
		createCustomTabB(CraftingGroup.REDSTONE, DoorBlock.class);
		createCustomTabC(CraftingGroup.REDSTONE, null, Items.DISPENSER, Items.DROPPER, Items.OBSERVER);
		createCustomTabC(CraftingGroup.REDSTONE, null, Items.TARGET, Items.TNT, Items.NOTE_BLOCK);

		createCustomTab(CraftingGroup.MISCELLANEOUS, matchRegexBlock(".*_wool"));
		createCustomTab(CraftingGroup.MISCELLANEOUS, matchRegexBlock(".*_terracotta"));
		createCustomTabB(CraftingGroup.MISCELLANEOUS, CarpetBlock.class);
		createCustomTabB(CraftingGroup.MISCELLANEOUS, StainedGlassPaneBlock.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, DyeItem.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, BannerItem.class);

		Minecraft.getInstance().getConnection().getRecipeManager().getRecipes().stream()
				.filter(c -> c.getType() == IRecipeType.CRAFTING).forEach(this::loadRecipe);

		selected = 0;

		getSelected().selected = true;

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

	public CraftingInventorySystem loadSize(int width, int height) {
		// Load the system
		loadIfRequired();

		itemMap.forEach((g, invGroup) -> invGroup.loadSize(width, height));

		return this;
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
