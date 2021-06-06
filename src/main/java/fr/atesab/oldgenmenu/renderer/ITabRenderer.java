package fr.atesab.oldgenmenu.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import fr.atesab.oldgenmenu.CraftingGroup;

@FunctionalInterface
public interface ITabRenderer {
	void render(CraftingGroup group, MatrixStack matrixStack, int x, int y, int w, int h);
}
