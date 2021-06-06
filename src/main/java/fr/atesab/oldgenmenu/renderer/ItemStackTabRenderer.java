package fr.atesab.oldgenmenu.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import fr.atesab.oldgenmenu.CraftingGroup;
import fr.atesab.oldgenmenu.RenderUtils;
import net.minecraft.item.ItemStack;

public class ItemStackTabRenderer implements ITabRenderer {
	public static ItemStackTabRenderer of(ItemStack stack) {
		return new ItemStackTabRenderer(stack);
	}

	private ItemStack stack;

	public ItemStackTabRenderer(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void render(CraftingGroup group, MatrixStack matrixStack, int x, int y, int w, int h) {
		RenderUtils.renderItem(stack, x, y, Math.min(w, h));
	}
}
