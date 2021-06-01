package fr.atesab.oldgenmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RenderUtils {
	private RenderUtils() {
	}

	/**
	 * Render an itemstack on the screen with a particular size
	 * 
	 * @param is    the stack to render
	 * @param x     x location of the item
	 * @param y     y location of the item
	 * @param width size of the item
	 */
	public static void renderItem(ItemStack is, int x, int y, int width) {
		Minecraft minecraft = Minecraft.getInstance();
		float scaleX = width / 16f;
		float scaleY = width / 16f;
		translatef(x, y, 0);
		scalef(scaleX, scaleY, 1f);
		ItemRenderer renderer = minecraft.getItemRenderer();
		renderer.renderAndDecorateFakeItem(is, 0, 0);
		scalef(1 / scaleX, 1 / scaleY, 1f);
		translatef(-x, -y, 0);
	}

	@SuppressWarnings("deprecation")
	public static void scalef(float x, float y, float z) {
		RenderSystem.scalef(x, y, z);
	}

	@SuppressWarnings("deprecation")
	public static void translatef(float x, float y, float z) {
		RenderSystem.translatef(x, y, z);
	}

	/**
	 * Render a scaled component
	 * 
	 * @param matrixStack
	 * @param x
	 * @param y
	 * @param height
	 * @param component
	 * @param color
	 * @return the text size
	 */
	public static int drawCenteredTextComponentScaled(MatrixStack matrixStack, int x, int y, int height,
			ITextComponent component, int color) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		float scaleX = (float) height / font.lineHeight;
		float scaleY = (float) height / font.lineHeight;
		translatef(x, y, 0);
		scalef(scaleX, scaleY, 1f);
		IReorderingProcessor ireorderingprocessor = component.getVisualOrderText();
		float size = font.width(ireorderingprocessor);
		font.draw(matrixStack, ireorderingprocessor, -size / 2.0f, 0f, color);
		scalef(1 / scaleX, 1 / scaleY, 1f);
		translatef(-x, -y, 0);
		return (int) (size * scaleX);
	}

	/**
	 * Render a scaled component
	 * 
	 * @param matrixStack
	 * @param x
	 * @param y
	 * @param height
	 * @param component
	 * @param color
	 * @return the text size
	 */
	public static int drawTextComponentScaled(MatrixStack matrixStack, int x, int y, int height,
			ITextComponent component, int color) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		float scaleX = (float) height / font.lineHeight;
		float scaleY = (float) height / font.lineHeight;
		translatef(x, y, 0);
		scalef(scaleX, scaleY, 1f);
		IReorderingProcessor ireorderingprocessor = component.getVisualOrderText();
		float size = font.width(ireorderingprocessor);
		font.draw(matrixStack, ireorderingprocessor, 0, 0f, color);
		scalef(1 / scaleX, 1 / scaleY, 1f);
		translatef(-x, -y, 0);
		return (int) (size * scaleX);
	}

	/**
	 * Render a scaled component
	 * 
	 * @param matrixStack
	 * @param x
	 * @param y
	 * @param height
	 * @param component
	 * @param color
	 * @return the text size
	 */
	public static int drawRightTextComponentScaled(MatrixStack matrixStack, int x, int y, int height,
			ITextComponent component, int color) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		float scaleX = (float) height / font.lineHeight;
		float scaleY = (float) height / font.lineHeight;
		translatef(x, y, 0);
		scalef(scaleX, scaleY, 1f);
		IReorderingProcessor ireorderingprocessor = component.getVisualOrderText();
		float size = font.width(ireorderingprocessor);
		font.draw(matrixStack, ireorderingprocessor, -size, 0f, color);
		scalef(1 / scaleX, 1 / scaleY, 1f);
		translatef(-x, -y, 0);
		return (int) (size * scaleX);
	}

	@SuppressWarnings("deprecation")
	public static void color4f(float r, float g, float b, float a) {
		RenderSystem.color4f(r, g, b, a);
	}

	@SuppressWarnings("deprecation")
	public static void color3f(float r, float g, float b) {
		RenderSystem.color3f(r, g, b);
	}

	public static void renderText(ResourceLocation resource, int x, int y, int w, int h) {
		RenderSystem.enableAlphaTest();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		Minecraft.getInstance().getTextureManager().bind(resource);
		RenderUtils.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.vertex(x, y, 0.0D).uv(0, 0).endVertex();
		bufferbuilder.vertex(x, y + h, 0.0D).uv(0, 1).endVertex();
		bufferbuilder.vertex(x + w, y + h, 0.0D).uv(1, 1).endVertex();
		bufferbuilder.vertex(x + w, y, 0.0D).uv(1, 0).endVertex();
		tessellator.end();
	}

}
