package fr.atesab.oldgenmenu;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class SlideButtonWidget extends AbstractButton {
	private static final Consumer<SlideButtonWidget> NO_ACTION = b -> {
	};
	private ResourceLocation in;
	private ResourceLocation out;
	private Consumer<SlideButtonWidget> pressable;

	public SlideButtonWidget(int x, int y, int w, int h, ResourceLocation in, ResourceLocation out,
			Consumer<SlideButtonWidget> pressable) {
		super(x, y, w, h, new StringTextComponent(""));
		this.in = in;
		this.out = out;
		this.pressable = pressable == null ? NO_ACTION : pressable;
	}

	public ResourceLocation getShowedResourceLocation() {
		return this.isHovered ? in : out;
	}

	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
		RenderUtils.renderText(getShowedResourceLocation(), x, y, getWidth(), getHeight());
	}

	@Override
	public void onPress() {
		pressable.accept(this);
	}

}
