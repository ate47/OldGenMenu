package fr.atesab.oldgenmenu;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class TabWidget extends AbstractButton {
	public static final ResourceLocation TAB_IN = new ResourceLocation("textures/gui/oldgen/tab_in.png");
	public static final ResourceLocation TAB_OUT = new ResourceLocation("textures/gui/oldgen/tab_out.png");
	private CraftingGroup group;
	private CraftingInventorySystem system;

	public TabWidget(int x, int y, int w, int h, CraftingGroup group, CraftingInventorySystem system) {
		super(x, y, w, h, new StringTextComponent(""));
		this.group = group;
		this.system = system;
	}

	@Override
	public void onPress() {
		system.select(group);
	}

	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
		RenderUtils.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderUtils.renderText(group.selected ? TAB_IN : TAB_OUT, x, y, width, height);
		RenderUtils.renderItem(group.getIcon(), x + 8, y + 12 - (group.selected ? 1 : 0), 16);
	}
}
