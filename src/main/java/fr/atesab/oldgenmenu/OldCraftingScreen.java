package fr.atesab.oldgenmenu;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
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
	private static final int GUI_WIDTH = 230;
	private static final int GUI_HEIGHT = 150;
	private RecipeBookContainer<CraftingInventory> recipeBookContainer;

	private int left = 0;
	private int top = 0;
	private int right = 0;
	private int bottom = 0;
	private OldGenMenuMod mod;
	private CraftingInventorySystem system;

	public OldCraftingScreen(RecipeBookContainer<CraftingInventory> recipeBookContainer, OldGenMenuMod mod) {
		super(new TranslationTextComponent("oldgenmenu.menu"));
		this.recipeBookContainer = recipeBookContainer;
		this.mod = mod;
		this.system = mod.system.loadIfRequired();
	}

	private void renderBg() {
		RenderUtils.renderText(OLDGEN_MENU, left, top, GUI_WIDTH, GUI_HEIGHT);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(matrixStack);
		renderBg();

		RenderUtils.drawCenteredTextComponentScaled(matrixStack, (left + right) / 2, top + 12, 10,
				system.getSelected().getDescription(), 0x5e5e5e);

		int tx = left + 11;
		int ty = top + 28;
		List<CraftingInventorySystemTab> a = system.getSysGroup().getTabs();
		for (int i = 0; i < a.size(); i++) {
			a.get(i).render(tx + i * 16, ty);
		}

		RenderUtils.drawCenteredTextComponentScaled(matrixStack, left + 120 + 105 / 2, top + 83, 6,
				new TranslationTextComponent("container.inventory"), 0x5e5e5e);

		int sx = recipeBookContainer.getGridWidth();
		int sy = recipeBookContainer.getGridHeight();

		int lx = left + 6 + 52 / 2 - sx * 16 / 2;
		int ly = top + 80 + 15 + 49 / 2 - sy * 16 / 2;

		for (int i = 0; i < sx; ++i)
			for (int j = 0; j < sy; ++j)
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
		}
		return super.keyPressed(key, mouseX, mouseY);
	}

	@Override
	protected void init() {
		left = (width - GUI_WIDTH) / 2;
		top = (height - GUI_HEIGHT) / 2;
		right = left + GUI_WIDTH;
		bottom = top + GUI_HEIGHT;
		int leftTab = left + GUI_WIDTH / 2 - 32 * CraftingGroup.GROUPS.size() / 2;
		int topTab = top - 32 + 3;
		for (int i = 0; i < CraftingGroup.GROUPS.size(); i++) {
			CraftingGroup g = CraftingGroup.GROUPS.get(i);
			addButton(new TabWidget(leftTab + i * 32, topTab, 32, 32, g, system));
		}
		addButton(new Button(0, 0, 200, 20, new StringTextComponent("reload"), b -> {
			system.reloadSystem();
		}));
		super.init();
	}

}
