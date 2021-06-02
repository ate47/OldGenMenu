package fr.atesab.oldgenmenu;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class OldCraftingScreen extends Screen {
	public static final ResourceLocation OLDGEN_MENU = new ResourceLocation("textures/gui/oldgen/oldgen.png");
	public static final ResourceLocation SLOT = new ResourceLocation("textures/gui/oldgen/slot.png");
	public static final ResourceLocation WARNING = new ResourceLocation("textures/gui/oldgen/warning.png");
	public static final ResourceLocation PAGE_BEFORE = new ResourceLocation("textures/gui/oldgen/page_before.png");
	public static final ResourceLocation PAGE_BEFORE_SELECTED = new ResourceLocation(
			"textures/gui/oldgen/page_before_selected.png");
	public static final ResourceLocation PAGE_AFTER = new ResourceLocation("textures/gui/oldgen/page_after.png");
	public static final ResourceLocation PAGE_AFTER_SELECTED = new ResourceLocation(
			"textures/gui/oldgen/page_after_selected.png");
	private static final int GUI_WIDTH = 230;
	private static final int GUI_HEIGHT = 150;
	private RecipeBookContainer<CraftingInventory> recipeBookContainer;

	private int left = 0;
	private int top = 0;
	private OldGenMenuMod mod;
	private CraftingInventorySystem system;

	public OldCraftingScreen(RecipeBookContainer<CraftingInventory> recipeBookContainer, OldGenMenuMod mod) {
		super(new TranslationTextComponent("oldgenmenu.menu"));
		this.recipeBookContainer = recipeBookContainer;
		this.mod = mod;
		this.system = mod.system;
	}

	private void renderBg() {
		RenderUtils.renderText(OLDGEN_MENU, left, top, GUI_WIDTH, GUI_HEIGHT);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(matrixStack);
		renderBg();

		RenderUtils.drawCenteredTextComponentScaled(matrixStack, left + GUI_WIDTH / 2, top + 12, 10,
				system.getSelected().getDescription(), 0x5e5e5e);

		int gridWidth = recipeBookContainer.getGridWidth();
		int gridHeight = recipeBookContainer.getGridHeight();

		int tx = left + 11;
		int ty = top + 28;
		List<CraftingInventorySystemTab> a = system.getSysGroup().getVisibleTabs();
		for (int i = 0; i < a.size(); i++) {
			a.get(i).render(tx + i * 16, ty, recipeBookContainer);
		}
		CraftingInventorySystemTab tab = system.getSysGroup().getSelected();
		if (tab != null) {
			ItemStorage is = tab.getSelected();
			if (is != null)
				RenderUtils.drawCenteredTextComponentScaled(matrixStack, left + 6 + 105 / 2, top + 83, 6,
						is.getItemStack().getHoverName(), 0x5e5e5e);
		}
		RenderUtils.drawCenteredTextComponentScaled(matrixStack, left + 120 + 105 / 2, top + 83, 6,
				new TranslationTextComponent("container.inventory"), 0x5e5e5e);

		int lx = left + 6 + 52 / 2 - gridWidth * 16 / 2;
		int ly = top + 80 + 15 + 49 / 2 - gridHeight * 16 / 2;

		for (int i = 0; i < gridWidth; ++i)
			for (int j = 0; j < gridHeight; ++j)
				RenderUtils.renderText(SLOT, lx + i * 16, ly + j * 16, 16, 16);

		int rx = left + 119 + 105 / 2 - 9 * 10 / 2;
		int ry = top + 80 + 12;

		for (int j = 0; j < 3; ++j)
			for (int i = 0; i < 9; ++i)
				RenderUtils.renderText(SLOT, rx + i * 10, ry + j * 10, 10, 10);

		for (int i = 0; i < 9; ++i)
			RenderUtils.renderText(SLOT, rx + i * 10, ry + 35, 10, 10);

		super.render(matrixStack, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean keyPressed(int key, int mouseX, int mouseY) {
		// try left page
		if (minecraft.options.keyLeft.getKey().getType() == InputMappings.Type.KEYSYM
				&& minecraft.options.keyLeft.getKey().getValue() == key) {
			// last item
			system.lastGroup();
			return true;
		} else if (minecraft.options.keyRight.getKey().getType() == InputMappings.Type.KEYSYM
				&& minecraft.options.keyRight.getKey().getValue() == key) {
			// next item
			system.nextGroup();
			return true;
		} else if (mod.getNextPageKey().getKey().getType() == InputMappings.Type.KEYSYM
				&& mod.getNextPageKey().getKey().getValue() == key) {
			// last item
			system.getSysGroup().lastItem();
			return true;
		} else if (mod.getLastPageKey().getKey().getType() == InputMappings.Type.KEYSYM
				&& mod.getLastPageKey().getKey().getValue() == key) {
			// next item
			system.getSysGroup().nextItem();
			return true;
		} else if (mod.getNextPageUp().getKey().getType() == InputMappings.Type.KEYSYM
				&& mod.getNextPageUp().getKey().getValue() == key) {
			// last item
			CraftingInventorySystemTab tab = system.getSysGroup().getSelected();
			if (tab != null)
				tab.nextItem();
			return true;
		} else if (mod.getLastPageDown().getKey().getType() == InputMappings.Type.KEYSYM
				&& mod.getLastPageDown().getKey().getValue() == key) {
			// next item
			CraftingInventorySystemTab tab = system.getSysGroup().getSelected();
			if (tab != null)
				tab.lastItem();
			return true;
		}
		return super.keyPressed(key, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		int leftObj = left + 11;
		if (mouseButton == 0 && RenderUtils.isIn((int) mouseX, (int) mouseY, leftObj, top + 28, 16 * 13, 16)) {
			system.getSysGroup().selectVisible((int) (mouseX - leftObj) / 16);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double factor) {
		if (factor > 0) {
			system.getSysGroup().nextItem();
		} else {
			system.getSysGroup().lastItem();
		}
		return super.mouseScrolled(mouseX, mouseY, factor);
	}

	@Override
	protected void init() {
		system.loadSize(recipeBookContainer.getGridWidth(), recipeBookContainer.getGridHeight());
		left = (width - GUI_WIDTH) / 2;
		top = (height - GUI_HEIGHT) / 2;
		int leftTab = left + GUI_WIDTH / 2 - 32 * CraftingGroup.GROUPS.size() / 2;
		int topTab = top - 32 + 3;
		for (int i = 0; i < CraftingGroup.GROUPS.size(); i++) {
			CraftingGroup g = CraftingGroup.GROUPS.get(i);
			addButton(new TabWidget(leftTab + i * 32, topTab, 32, 32, g, system));
		}
		addButton(new Button(0, 0, 200, 20, new StringTextComponent("reload"), b -> {
			system.reloadSystem();
			system.loadSize(recipeBookContainer.getGridWidth(), recipeBookContainer.getGridHeight());
		}));
		addButton(new SlideButtonWidget(left + 3, top + 28, 8, 16, PAGE_BEFORE_SELECTED, PAGE_BEFORE,
				b -> system.getSysGroup().lastItem()));
		addButton(new SlideButtonWidget(left + 219, top + 28, 8, 16, PAGE_AFTER_SELECTED, PAGE_AFTER,
				b -> system.getSysGroup().nextItem()));
		super.init();
	}

}
