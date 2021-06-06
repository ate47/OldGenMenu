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
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
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
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.HorseArmorItem;
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
import net.minecraft.item.SignItem;
import net.minecraft.item.SoupItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.WallOrFloorItem;
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
			createCustomTab(g, i -> iSet.contains(i));
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
		if (stack.getItem() == Items.AIR)
			return;
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
				Items.POLISHED_DIORITE, Items.POLISHED_GRANITE, Items.PURPUR_BLOCK, Items.PURPUR_PILLAR,
				Items.CHISELED_NETHER_BRICKS, Items.CRACKED_NETHER_BRICKS, Items.CHISELED_POLISHED_BLACKSTONE,
				Items.CUT_RED_SANDSTONE, Items.POLISHED_BLACKSTONE_BRICKS, Items.CHISELED_SANDSTONE,
				Items.CHISELED_RED_SANDSTONE);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.SANDSTONE, Items.RED_SANDSTONE, Items.SMOOTH_SANDSTONE,
				Items.CUT_SANDSTONE, Items.SMOOTH_RED_SANDSTONE, Items.NETHER_BRICK, Items.COARSE_DIRT,
				Items.PRISMARINE_BRICKS, Items.PRISMARINE_BRICKS, Items.SNOW_BLOCK, Items.SNOW, Items.CLAY, Items.BRICK,
				Items.BONE_BLOCK);
		createCustomTabB(CraftingGroup.STRUCTURES, WallBlock.class);

		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.CRAFTING_TABLE, Items.FURNACE, Items.ENCHANTING_TABLE,
				Items.ANVIL, Items.BREWING_STAND, Items.FLETCHING_TABLE, Items.SMITHING_TABLE, Items.GRINDSTONE,
				Items.SCAFFOLDING, Items.BLAST_FURNACE, Items.CARTOGRAPHY_TABLE);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST,
				Items.SHULKER_BOX, Items.BARREL);
		createCustomTab(CraftingGroup.STRUCTURES, matchRegexBlock(".*_wood"));

		createCustomTabC(CraftingGroup.STRUCTURES, BedItem.class);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.DIAMOND_BLOCK,
				Items.EMERALD_BLOCK, Items.NETHERITE_BLOCK, Items.LAPIS_BLOCK, Items.COAL_BLOCK);
		createCustomTabB(CraftingGroup.STRUCTURES, StairsBlock.class);
		createCustomTabB(CraftingGroup.STRUCTURES, SlabBlock.class);
		createCustomTabB(CraftingGroup.STRUCTURES, AbstractGlassBlock.class, Items.GLASS);
		createCustomTabB(CraftingGroup.STRUCTURES, StainedGlassPaneBlock.class, Items.GLASS_PANE);
		createCustomTabB(CraftingGroup.STRUCTURES, ConcretePowderBlock.class);

		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.PRISMARINE, Items.PRISMARINE_BRICKS,
				Items.DARK_PRISMARINE, Items.PRISMARINE_WALL);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE);
		createCustomTabC(CraftingGroup.STRUCTURES, null, Items.GLOWSTONE, Items.GLOWSTONE_DUST, Items.SEA_LANTERN);

		createCustomTabC(CraftingGroup.TOOLS, SwordItem.class);
		createCustomTabC(CraftingGroup.TOOLS, PickaxeItem.class);
		createCustomTabC(CraftingGroup.TOOLS, ShovelItem.class);
		createCustomTabC(CraftingGroup.TOOLS, AxeItem.class);
		createCustomTabC(CraftingGroup.TOOLS, HoeItem.class);

		createCustomTabC(CraftingGroup.TOOLS, null, Items.FLINT_AND_STEEL, Items.SHEARS, Items.BUCKET);
		createCustomTabC(CraftingGroup.TOOLS, ShootableItem.class);
		createCustomTabC(CraftingGroup.TOOLS, ArrowItem.class);
		createCustomTabC(CraftingGroup.TOOLS, null, Items.COMPASS, Items.CLOCK);
		createCustomTabC(CraftingGroup.TOOLS, ShieldItem.class);
		createCustomTabC(CraftingGroup.TOOLS, FishingRodItem.class);

		createCustomTabC(CraftingGroup.FOOD, null, Items.GLASS_BOTTLE, Items.HONEY_BOTTLE, Items.HONEYCOMB,
				Items.HONEY_BLOCK, Items.HONEYCOMB_BLOCK, Items.BEEHIVE);
		createCustomTabC(CraftingGroup.FOOD, null, Items.COOKIE, Items.CAKE);
		createCustomTabC(CraftingGroup.FOOD, null, Items.SUGAR, Items.FERMENTED_SPIDER_EYE);
		createCustomTabC(CraftingGroup.FOOD, null, Items.GOLDEN_CARROT);
		createCustomTabC(CraftingGroup.FOOD, null, Items.BONE_MEAL);
		createCustomTabC(CraftingGroup.FOOD, null, Items.KELP, Items.DRIED_KELP_BLOCK, Items.DRIED_KELP);
		createCustomTabC(CraftingGroup.FOOD, SoupItem.class, Items.BOWL);
		createCustomTabC(CraftingGroup.FOOD, null, Items.WHEAT, Items.WHEAT_SEEDS, Items.HAY_BLOCK, Items.BREAD);
		createCustomTabC(CraftingGroup.FOOD, null, Items.PUMPKIN, Items.PUMPKIN_PIE, Items.JACK_O_LANTERN,
				Items.PUMPKIN_SEEDS);
		createCustomTabC(CraftingGroup.FOOD, null, Items.MELON, Items.MELON_SEEDS, Items.MELON_SLICE,
				Items.GLISTERING_MELON_SLICE);
		createCustomTabC(CraftingGroup.FOOD, null, Items.LEATHER);
		createCustomTabC(CraftingGroup.FOOD, null, Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);

		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.HEAD));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.CHEST));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.LEGS));
		createCustomTab(CraftingGroup.ARMORS, matchArmorItem(EquipmentSlotType.FEET));
		createCustomTabC(CraftingGroup.ARMORS, HorseArmorItem.class);

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

		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.CAMPFIRE, Items.COMPOSTER, Items.SMOKER,
				Items.SOUL_CAMPFIRE, Items.CAULDRON, Items.LOOM, Items.STONECUTTER, Items.LODESTONE);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.TORCH, Items.SOUL_LANTERN, Items.SOUL_TORCH,
				Items.LANTERN);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND,
				Items.EMERALD, Items.NETHERITE_INGOT, Items.LAPIS_LAZULI, Items.IRON_NUGGET, Items.GOLD_NUGGET,
				Items.COAL);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, DyeItem.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, BannerItem.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, BannerPatternItem.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, SignItem.class, Items.ARMOR_STAND, Items.ITEM_FRAME,
				Items.PAINTING);
		createCustomTabB(CraftingGroup.MISCELLANEOUS, FenceBlock.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.CONDUIT, Items.BEACON, Items.END_CRYSTAL);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.ENDER_EYE, Items.BLAZE_POWDER, Items.FIRE_CHARGE);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.MAGMA_CREAM, Items.SLIME_BALL, Items.SLIME_BLOCK);
		createCustomTab(CraftingGroup.MISCELLANEOUS, matchRegexBlock(".*_terracotta"));
		createCustomTab(CraftingGroup.MISCELLANEOUS, matchRegexBlock(".*_wool"));
		createCustomTabB(CraftingGroup.MISCELLANEOUS, CarpetBlock.class);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.PAPER, Items.BOOK, Items.BOOKSHELF,
				Items.WRITABLE_BOOK, Items.WRITABLE_BOOK);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.MAP);
		createCustomTabC(CraftingGroup.MISCELLANEOUS, null, Items.FLOWER_POT);

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

	public int getSelectedIndex() {
		return selected;
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
