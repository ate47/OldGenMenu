package fr.atesab.oldgenmenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OldGenMenuMod.MOD_ID)
public class OldGenMenuMod {
	public static final String MOD_ID = "oldgenmenu";
	public static final String MOD_NAME = "Old Gen Menu";
	private static final Logger LOGGER = LogManager.getLogger();
	public final CraftingInventorySystem system = new CraftingInventorySystem();

	private KeyBinding nextPage;
	private KeyBinding lastPage;
	private KeyBinding nextPageUp;
	private KeyBinding lastPageDown;

	public OldGenMenuMod() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
	}

	private void commonSetup(FMLCommonSetupEvent ev) {
		nextPage = new KeyBinding("key.oldgenmenu.last", GLFW.GLFW_KEY_LEFT, MOD_NAME);
		lastPage = new KeyBinding("key.oldgenmenu.next", GLFW.GLFW_KEY_RIGHT, MOD_NAME);
		nextPageUp = new KeyBinding("key.oldgenmenu.up", GLFW.GLFW_KEY_UP, MOD_NAME);
		lastPageDown = new KeyBinding("key.oldgenmenu.down", GLFW.GLFW_KEY_DOWN, MOD_NAME);

		ClientRegistry.registerKeyBinding(nextPage);
		ClientRegistry.registerKeyBinding(lastPage);
		ClientRegistry.registerKeyBinding(nextPageUp);
		ClientRegistry.registerKeyBinding(lastPageDown);
	}

	/**
	 * @return the nextPage
	 */
	public KeyBinding getNextPageKey() {
		return nextPage;
	}

	/**
	 * @return the lastPage
	 */
	public KeyBinding getLastPageKey() {
		return lastPage;
	}

	/**
	 * @return the lastPageDown
	 */
	public KeyBinding getLastPageDown() {
		return lastPageDown;
	}

	/**
	 * @return the nextPageUp
	 */
	public KeyBinding getNextPageUp() {
		return nextPageUp;
	}

	@SubscribeEvent
	public void onInitMenu(GuiScreenEvent.InitGuiEvent.Post ev) {
		LOGGER.info(ev.getGui().getClass());
		ContainerScreen<? extends RecipeBookContainer<CraftingInventory>> container;
		Screen gui = ev.getGui();
		if (ev.getGui() instanceof CraftingScreen) {
			container = ((CraftingScreen) gui);
		} else if (ev.getGui() instanceof InventoryScreen) {
			container = ((InventoryScreen) gui);
		} else
			return;

		RecipeBookContainer<CraftingInventory> c = container.getMenu();

		int right = container.getGuiLeft() + container.getXSize();
		int top = container.getGuiTop();

		ev.addWidget(new Button(right + 2, top + 2, 20, 20, new TranslationTextComponent("oldgenmenu.open.little"),
				b -> Minecraft.getInstance().setScreen(new OldCraftingScreen(c, this))));
	}
}
