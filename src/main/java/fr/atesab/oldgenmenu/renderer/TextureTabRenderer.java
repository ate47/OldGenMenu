package fr.atesab.oldgenmenu.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import fr.atesab.oldgenmenu.CraftingGroup;
import fr.atesab.oldgenmenu.RenderUtils;
import net.minecraft.util.ResourceLocation;

public class TextureTabRenderer implements ITabRenderer {
	private ResourceLocation resourceLocation;
	private float u;
	private float v;
	private float uw;
	private float vh;

	public TextureTabRenderer(ResourceLocation resourceLocation, float u, float v, float uw, float vh) {
		this.resourceLocation = resourceLocation;
		this.u = u;
		this.v = v;
		this.uw = uw;
		this.vh = vh;
	}

	public TextureTabRenderer(ResourceLocation resourceLocation, int x, int y, int imageWidth, int imageHeight) {
		this(resourceLocation, (float) x / imageWidth, (float) y / imageHeight, 1f / imageWidth, 1f / imageHeight);
	}

	@Override
	public void render(CraftingGroup group, MatrixStack matrixStack, int x, int y, int w, int h) {
		RenderUtils.renderText(resourceLocation, x, y, w, h, u, v, uw, vh, 0xffffffff);
	}
}
