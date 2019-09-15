package com.lothrazar.storagenetwork.gui;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.api.IGuiPrivate;
import com.lothrazar.storagenetwork.api.data.EnumSortType;
import com.lothrazar.storagenetwork.api.util.UtilTileEntity;
import com.lothrazar.storagenetwork.block.request.ContainerRequest;
import com.lothrazar.storagenetwork.block.request.GuiButtonRequest;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.jei.JeiSettings;
import com.lothrazar.storagenetwork.network.ClearRecipeMessage;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.network.SortMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

/**
 * Base class for Request table inventory and Remote inventory
 */
public class GuiRequestTable extends ContainerScreen<ContainerRequest> implements IGuiPrivate, IGuiNetwork {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request.png");
  private ItemStack stackUnderMouse = ItemStack.EMPTY;
  private TextFieldWidget searchBar;
  private boolean forceFocus;
  private GuiButtonRequest directionBtn, sortBtn, jeiBtn, clearTextBtn;
  final NetworkWidget network;
  private TileRequest tile;

  public GuiRequestTable(ContainerRequest container, PlayerInventory inv, ITextComponent name) {
    super(container, inv, name);
    tile = container.getTileRequest();
    network = new NetworkWidget();
    xSize = WIDTH;
    ySize = HEIGHT;
  }

  @Override public void setStacks(List<ItemStack> stacks) {
    network.stacks = stacks;
  }

  @Override
  public void init() {
    super.init();
    //    Keyboard.enableRepeatEvents(true);
    searchBar = new TextFieldWidget(font, guiLeft + 81, guiTop + 96, 85, font.FONT_HEIGHT, "search");
    searchBar.setMaxStringLength(30);
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused2(true);
    if (JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearchSynced()) {
      searchBar.setText(JeiHooks.getFilterText());
    }
    int y = searchBar.y - 3;
    directionBtn = new GuiButtonRequest(guiLeft + 7, y, "", (p) -> {
      this.setDownwards(!this.getDownwards());
      this.syncData();
    });
    directionBtn.setHeight(16);
    addButton(directionBtn);
    sortBtn = new GuiButtonRequest(guiLeft + 21, y, "", (p) -> {
      this.setSort(this.getSort().next());
      this.syncData();
    });
    sortBtn.setHeight(16);
    addButton(sortBtn);
    jeiBtn = new GuiButtonRequest(guiLeft + 35, y, "", (p) -> {
      JeiSettings.setJeiSearchSync(!JeiSettings.isJeiSearchSynced());
    });
    jeiBtn.setHeight(16);
    if (JeiSettings.isJeiLoaded()) {
      addButton(jeiBtn);
    }
    clearTextBtn = new GuiButtonRequest(guiLeft + 64, y, "X", (p) -> {
      this.clearSearch();
    });
    clearTextBtn.setHeight(16);
    addButton(clearTextBtn);
  }

  private void syncData() {
    PacketRegistry.INSTANCE.sendToServer(new SortMessage(getPos(), getDownwards(), getSort()));
  }

  public boolean getDownwards() {
    return tile.isDownwards();
  }

  public void setDownwards(boolean d) {
    tile.setDownwards(d);
  }

  public EnumSortType getSort() {
    return tile.getSort();
  }

  public void setSort(EnumSortType s) {
    tile.setSort(s);
  }

  public BlockPos getPos() {
    return tile.getPos();
  }

  private static int getDim() {
    return 0;//TODO
  }

  private boolean inField(int mouseX, int mouseY) {
    int h = 90;
    return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + h);
  }

  private boolean inSearchbar(double mouseX, double mouseY) {
    return isPointInRegion(searchBar.x - guiLeft + 14,
        searchBar.y - guiTop,
        searchBar.getWidth(), font.FONT_HEIGHT + 6,
        mouseX, mouseY);
  }

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getText();
    if (searchText.startsWith("@")) {
      String name = UtilTileEntity.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("#")) {
      String tooltipString;
      Minecraft mc = Minecraft.getInstance();
      List<ITextComponent> tooltip = stack.getTooltip(mc.player, TooltipFlags.NORMAL);
      tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
      //      tooltipString = ChatFormatting.stripFormatting(tooltipString);
      return tooltipString.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    //TODO : tag search?
    //    else if (searchText.startsWith("$")) {
    //      StringBuilder oreDictStringBuilder = new StringBuilder();
    //      for (int oreId : OreDictionary.getOreIDs(stack)) {
    //        String oreName = OreDictionary.getOreName(oreId);
    //        oreDictStringBuilder.append(oreName).append(' ');
    //      }
    //      return oreDictStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    //    }
    //      return creativeTabStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    //    }
    else {
      return stack.getDisplayName().toString().toLowerCase().contains(searchText.toLowerCase());
    }
  }

  @Override
  public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    renderTextures();
    List<ItemStack> stacksToDisplay = applySearchTextToSlots();
    sortStackWrappers(stacksToDisplay);
    network.applyScrollPaging(stacksToDisplay);
    network.rebuildItemSlots(stacksToDisplay, this);
    renderItemSlots(mouseX, mouseY);
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    renderBackground();
    super.render(mouseX, mouseY, partialTicks);
    renderHoveredToolTip(mouseX, mouseY);
    searchBar.render(mouseX, mouseY, partialTicks);
  }

  private void renderTextures() {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(texture);
    int xCenter = (width - xSize) / 2;
    int yCenter = (height - ySize) / 2;
    blit(xCenter, yCenter, 0, 0, xSize, ySize);
  }

  private List<ItemStack> applySearchTextToSlots() {
    String searchText = searchBar.getText();

    List<ItemStack> stacksToDisplay = searchText.equals("") ? Lists.newArrayList(network.stacks) : Lists.newArrayList();
    if (!searchText.equals("")) {
      for (ItemStack stack : network.stacks) {
        if (doesStackMatchSearch(stack)) {
          stacksToDisplay.add(stack);
        }
      }
    }
    return stacksToDisplay;
  }

  private void renderItemSlots(int mouseX, int mouseY) {
    stackUnderMouse = ItemStack.EMPTY;
    for (ItemSlotNetwork slot : network.slots) {
      slot.drawSlot(font, mouseX, mouseY);
      if (slot.isMouseOverSlot(mouseX, mouseY)) {
        stackUnderMouse = slot.getStack();
      }
    }
    if (network.slots.isEmpty()) {
      stackUnderMouse = ItemStack.EMPTY;
    }
  }

  private void sortStackWrappers(List<ItemStack> stacksToDisplay) {
    Collections.sort(stacksToDisplay, new Comparator<ItemStack>() {

      final int mul = getDownwards() ? -1 : 1;

      @Override
      public int compare(ItemStack o2, ItemStack o1) {
        switch (getSort()) {
          case AMOUNT:
            return Integer.compare(o1.getCount(), o2.getCount()) * mul;
          case NAME:
            return o2.getDisplayName().toString().compareToIgnoreCase(o1.getDisplayName().toString()) * mul;
          case MOD:
            return UtilTileEntity.getModNameForItem(o2.getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getItem())) * mul;
        }
        return 0;
      }
    });
  }

  @Override
  public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    if (forceFocus && searchBar != null) {
      searchBar.setFocused2(true);
      if (searchBar.isFocused()) {
        forceFocus = false;
      }
    }
    this.directionBtn.setMessage(this.getDownwards() ? "D" : "U");
    String sort = "";
    switch (this.getSort()) {
      case NAME:
        sort = "N";
        break;
      case MOD:
        sort = "@";
        break;
      case AMOUNT:
        sort = "#";
        break;
    }
    this.sortBtn.setMessage(sort);
    jeiBtn.setMessage(JeiSettings.isJeiSearchSynced() ? "J" : "-");
    drawTooltips(mouseX, mouseY);
    for (ItemSlotNetwork s : network.slots) {
      if (s != null && s.isMouseOverSlot(mouseX, mouseY)) {
        s.drawTooltip(mouseX, mouseY);
      }
    }
  }

  private void drawTooltips(final int mouseX, final int mouseY) {
    if (clearTextBtn != null && clearTextBtn.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.tooltip_clear")), mouseX - guiLeft, mouseY - this.guiTop);
    }
    if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.req.tooltip_" + getSort())), mouseX - this.guiLeft, mouseY - this.guiTop);
    }
    if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.sort")), mouseX - this.guiLeft, mouseY - this.guiTop);
    }
    if (JeiSettings.isJeiLoaded() && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
      String s = I18n.format(JeiSettings.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
      renderTooltip(Lists.newArrayList(s), mouseX - guiLeft, mouseY - this.guiTop);
    }

    if (inSearchbar(mouseX, mouseY)) {
      List<String> lis = Lists.newArrayList();
      if (!Screen.hasShiftDown()) {
        lis.add(I18n.format("gui.storagenetwork.shift"));
      }
      else {
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_0"));//@
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_1"));//#
        //TODO: tag search
        //        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_2"));//$
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_3"));//clear
      }
      renderTooltip(lis, mouseX - this.guiLeft, mouseY - this.guiTop);
    }
  }

   

  private void clearSearch() {
    if (searchBar == null) {
      return;
    }
    searchBar.setText("");
    if (JeiSettings.isJeiSearchSynced()) {
      JeiHooks.setFilterText("");
    }
  }

   boolean isScrollable(double x, double y) {
    return isPointInRegion(0, 0,
        this.width - 8, 135,
        x, y);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double mouseButton) {
    super.mouseScrolled(x, y, mouseButton);
    //<0 going down
    // >0 going up
    if (isScrollable(x, y) && mouseButton != 0) {
      network.mouseScrolled(mouseButton);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    searchBar.setFocused2(false);
    int rectX = 63;
    int rectY = 110;
    if (inSearchbar(mouseX, mouseY)) {
      searchBar.setFocused2(true);
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        clearSearch();
      }
    }
    else if (isPointInRegion(rectX, rectY, 7, 7, mouseX, mouseY)) {
      PacketRegistry.INSTANCE.sendToServer(new ClearRecipeMessage());
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
    }
    else if (searchBar.mouseClicked(mouseX, mouseY, mouseButton)) {
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        this.clearSearch();
      }
      return true;
    }
    else {
      ItemStack stackCarriedByMouse = minecraft.player.inventory.getItemStack();
      if (!stackUnderMouse.isEmpty()
          && (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT || mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT)
          && stackCarriedByMouse.isEmpty() &&
          network.canClick()) {
        ItemStack copyNotNegativeAir = new ItemStack(stackUnderMouse.getItem());
        PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, copyNotNegativeAir, Screen.hasShiftDown(),
            Screen.hasAltDown() || Screen.hasControlDown()));
        network.lastClick = System.currentTimeMillis();
      }
      else if (!stackCarriedByMouse.isEmpty() && inField((int) mouseX, (int) mouseY) &&
          network.canClick()) {
        PacketRegistry.INSTANCE.sendToServer(new InsertMessage(getDim(), mouseButton));
        network.lastClick = System.currentTimeMillis();
      }
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int b) {
    InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
    if (keyCode == 256) {
      minecraft.player.closeScreen();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    if (searchBar.isFocused()) {
      searchBar.keyPressed(keyCode, scanCode, b);
      return true;
    }
    else if (this.stackUnderMouse.isEmpty()) {
      try {
        System.out.println("jei key " + mouseKey);
        JeiHooks.testJeiKeybind(mouseKey, this.stackUnderMouse);
      }
      catch (Throwable e) {
        System.out.println("JEI compat issue " + e);
        //its ok JEI not installed for maybe an addon mod is ok
      }
    }
    //regardles of above branch, also check this
    if (minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
      minecraft.player.closeScreen();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    return super.keyPressed(keyCode, scanCode, b);
  }

  @Override
  public boolean charTyped(char typedChar, int keyCode) {
    //    super.keyPressed()
    //func_195363_d
    if (searchBar.isFocused() && searchBar.charTyped(typedChar, keyCode)) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      if (JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearchSynced()) {
        JeiHooks.setFilterText(searchBar.getText());
      }
      return true;
    }
    else if (stackUnderMouse.isEmpty() == false) {
      try {
        //          JeiHooks.testJeiKeybind(keyCode, stackUnderMouse);
      }
      catch (Throwable e) {
        //its ok JEI not installed for maybe an addon mod is ok
      }
    }
    else {
      //      super.keyPressed(typedChar, keyCode, whatami);
    }
    //    }
    return false;// super.charTyped(typedChar, keyCode);
  }

  @Override
  public boolean isInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
    return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  @Override
  public void renderStackToolTip(ItemStack stack, int x, int y) {
    super.renderTooltip(stack, x, y);
  }

  @Override
  public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
    super.fillGradient(left, top, right, bottom, startColor, endColor);
  }
}