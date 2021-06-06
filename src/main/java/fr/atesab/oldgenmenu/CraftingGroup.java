package fr.atesab.oldgenmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import fr.atesab.oldgenmenu.renderer.ITabRenderer;
import fr.atesab.oldgenmenu.renderer.ItemStackTabRenderer;
import fr.atesab.oldgenmenu.renderer.TextureTabRenderer;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CraftingGroup {
	public static final ResourceLocation OLDGEN_ICONS = new ResourceLocation("textures/gui/oldgen/icons.png");
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
		STRUCTURES = new CraftingGroup("oldgenmenu.menu.structures",
				new ItemStackTabRenderer(new ItemStack(Blocks.OAK_PLANKS)));
		TOOLS = new CraftingGroup("oldgenmenu.menu.tools", new TextureTabRenderer(OLDGEN_ICONS, 0, 0, 4, 4));
		FOOD = new CraftingGroup("oldgenmenu.menu.food", new TextureTabRenderer(OLDGEN_ICONS, 1, 0, 4, 4));
		ARMORS = new CraftingGroup("oldgenmenu.menu.armors", new TextureTabRenderer(OLDGEN_ICONS, 2, 0, 4, 4));
		REDSTONE = new CraftingGroup("oldgenmenu.menu.redstone",
				new ItemStackTabRenderer(new ItemStack(Items.REDSTONE)));
		TRANSPORT = new CraftingGroup("oldgenmenu.menu.transport",
				new ItemStackTabRenderer(new ItemStack(Blocks.RAIL)));
		MISCELLANEOUS = new CraftingGroup("oldgenmenu.menu.miscellaneous",
				new ItemStackTabRenderer(new ItemStack(Items.PAINTING)));
	}

	private ITextComponent description;
	private ITabRenderer renderer;
	public boolean selected = false;
	private int id;

	public CraftingGroup(String descriptionId, ITabRenderer renderer) {
		this.id = ++idCounter;
		this.description = new TranslationTextComponent(descriptionId);
		this.renderer = renderer;
		GROUPS_INTERNAL.add(this);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public CraftingGroup(String description, Supplier<ITabRenderer> icon) {
		this(description, icon.get());
	}

	/**
	 * @return the icon
	 */
	public ITabRenderer getIcon() {
		return renderer;
	}

	/**
	 * @return the description
	 */
	public ITextComponent getDescription() {
		return description;
	}
}
