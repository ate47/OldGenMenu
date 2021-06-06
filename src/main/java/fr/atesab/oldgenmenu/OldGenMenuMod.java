package fr.atesab.oldgenmenu;

import java.util.function.Supplier;

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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
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
	private KeyBinding openOldGenCrafting;

	public OldGenMenuMod() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
	}

	private void commonSetup(FMLCommonSetupEvent ev) {
		nextPage = new KeyBinding("key.oldgenmenu.last", GLFW.GLFW_KEY_LEFT, MOD_NAME);
		lastPage = new KeyBinding("key.oldgenmenu.next", GLFW.GLFW_KEY_RIGHT, MOD_NAME);
		nextPageUp = new KeyBinding("key.oldgenmenu.up", GLFW.GLFW_KEY_UP, MOD_NAME);
		lastPageDown = new KeyBinding("key.oldgenmenu.down", GLFW.GLFW_KEY_DOWN, MOD_NAME);
		openOldGenCrafting = new KeyBinding("key.oldgenmenu.craft", GLFW.GLFW_KEY_Y, MOD_NAME);

		ClientRegistry.registerKeyBinding(nextPage);
		ClientRegistry.registerKeyBinding(lastPage);
		ClientRegistry.registerKeyBinding(nextPageUp);
		ClientRegistry.registerKeyBinding(lastPageDown);
		ClientRegistry.registerKeyBinding(openOldGenCrafting);
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
		Supplier<Screen> menu;
		Screen gui = ev.getGui();
		int right;
		int top;
		if (ev.getGui() instanceof CraftingScreen) {
			CraftingScreen container = ((CraftingScreen) gui);
			PlayerInventory inventory = Minecraft.getInstance().player.inventory;
			right = container.getGuiLeft() + container.getXSize();
			top = container.getGuiTop();
			menu = () -> new OldCraftingScreen<WorkbenchContainer>(container.getMenu(), inventory, this);
		} else if (ev.getGui() instanceof InventoryScreen) {
			InventoryScreen container = ((InventoryScreen) gui);
			PlayerInventory inventory = Minecraft.getInstance().player.inventory;
			right = container.getGuiLeft() + container.getXSize();
			top = container.getGuiTop();
			menu = () -> new OldCraftingScreen<PlayerContainer>(container.getMenu(), inventory, this);
		} else
			return;

		ev.addWidget(new Button(right + 2, top + 2, 20, 20, new TranslationTextComponent("oldgenmenu.open.little"),
				b -> Minecraft.getInstance().setScreen(menu.get())));
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent ev) {
		if (openOldGenCrafting.consumeClick()) {
			Minecraft mc = Minecraft.getInstance();
			InventoryScreen container = new InventoryScreen(mc.player);
			mc.setScreen(new OldCraftingScreen<PlayerContainer>(container.getMenu(), mc.player.inventory, this));
		}
	}
}
