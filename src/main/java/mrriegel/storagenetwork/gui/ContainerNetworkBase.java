package mrriegel.storagenetwork.gui;

import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.network.StacksMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.data.FilterItem;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public abstract class ContainerNetworkBase extends Container {

  public InventoryPlayer playerInv;
  protected InventoryCraftResult result;
  protected InventoryCraftingNetwork matrix;
  public boolean recipeLocked = false;

  public InventoryCrafting getCraftMatrix() {
    return this.matrix;
  }

  public abstract TileMaster getTileMaster();

  public abstract void bindHotbar();

  public abstract void slotChanged();

  boolean test = false;

  protected void bindPlayerInvo(final InventoryPlayer playerInv) {
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
  }

  protected void bindGrid() {
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
  }

  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    super.onCraftMatrixChanged(inventoryIn);
  }

  protected void findMatchingRecipe(InventoryCrafting craftMatrix) {
    IRecipe recipe = null;
    try {
      StorageNetwork.benchmark("findMatchingRecipe start");
      recipe = CraftingManager.findMatchingRecipe(matrix, this.playerInv.player.world);
    }
    catch (java.util.NoSuchElementException err) {
      // this seems basically out of my control, its DEEP in vanilla and some library, no idea whats up with that 
      // https://pastebin.com/2S9LSe23
      StorageNetwork.instance.logger.error("Error finding recipe [0] Possible conflict with forge, vanilla, or Storage Network", err);
    }
    catch (Throwable e) {
      StorageNetwork.instance.logger.error("Error finding recipe [-1]", e);
    }
    if (recipe != null) {
      StorageNetwork.benchmark("findMatchingRecipe end success");
      ItemStack itemstack = recipe.getCraftingResult(this.matrix);
      //real way to not lose nbt tags BETTER THAN COPY 
      this.result.setInventorySlotContents(0, itemstack);
    }
    else {
      StorageNetwork.benchmark("findMatchingRecipe end fail ");
      this.result.setInventorySlotContents(0, ItemStack.EMPTY);
    }
  }

  /**
   * A note on the shift-craft delay bug root cause was ANY interaction with matrix (setting contents etc) was causing triggers/events to do a recipe lookup. Meaning during this shift-click action you
   * can get up to 9x64 FULL recipe scans Solution is just to disable all those triggers but only for duration of this action
   * 
   * @param player
   * @param tile
   */
  protected void craftShift(EntityPlayer player, TileMaster tile) {
    IRecipe recipeCurrent = CraftingManager.findMatchingRecipe(matrix, player.world);
    if (recipeCurrent == null) {
      return;
    }
    this.recipeLocked = true;
    int crafted = 0;
    List<ItemStack> recipeCopy = Lists.newArrayList();
    for (int i = 0; i < matrix.getSizeInventory(); i++) {
      recipeCopy.add(matrix.getStackInSlot(i).copy());
    }
    ItemStack res = recipeCurrent.getCraftingResult(matrix);
    if (res.isEmpty()) {
      StorageNetwork.instance.logger.error("Recipe output is an empty stack " + recipeCurrent);
      return;
    }
    int sizePerCraft = res.getCount();
    int sizeFull = res.getMaxStackSize();
    int numberToCraft = sizeFull / sizePerCraft;
    StorageNetwork.log("numberToCraft = " + numberToCraft + " for stack " + res);
    while (crafted + sizePerCraft <= res.getMaxStackSize()) {
      res = recipeCurrent.getCraftingResult(matrix);
      StorageNetwork.log("crafted = " + crafted + " ; res.count() = " + res.getCount() + " MAX=" + res.getMaxStackSize());
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res, true).isEmpty()) {
        break;
      }
      //stop if empty
      if (recipeCurrent.matches(matrix, player.world) == false) {
        break;
      }
      //onTake replaced with this handcoded rewrite
      StorageNetwork.log("addItemStackToInventory " + res);
      if (!player.inventory.addItemStackToInventory(res)) {
        player.dropItem(res, false);
      }
      NonNullList<ItemStack> remainder = CraftingManager.getRemainingItems(matrix, player.world);
      for (int i = 0; i < remainder.size(); ++i) {
        ItemStack slot = this.matrix.getStackInSlot(i);
        ItemStack remainderCurrent = remainder.get(i);
        if (slot.getItem().getContainerItem() != null) { //is the fix for milk and similar
          slot = new ItemStack(slot.getItem().getContainerItem());
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!slot.getItem().getContainerItem(slot).isEmpty()) { //is the fix for milk and similar
          slot = slot.getItem().getContainerItem(slot);
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!remainderCurrent.isEmpty()) {
          if (slot.isEmpty()) {
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else if (ItemStack.areItemsEqual(slot, remainderCurrent) && ItemStack.areItemStackTagsEqual(slot, remainderCurrent)) {
            remainderCurrent.grow(slot.getCount());
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else if (ItemStack.areItemsEqualIgnoreDurability(slot, remainderCurrent)) {
            //crafting that consumes durability
            StorageNetwork.benchmark("fix for crafting eating durability");
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else {
            StorageNetwork.log("Gadd to inventory " + remainderCurrent);
            if (!player.inventory.addItemStackToInventory(remainderCurrent)) {
              player.dropItem(remainderCurrent, false);
            }
          }
        }
        else if (!slot.isEmpty()) {
          this.matrix.decrStackSize(i, 1);
          slot = this.matrix.getStackInSlot(i);
        }
      }
      //END onTake redo 
      crafted += sizePerCraft;
      ItemStack stackInSlot;
      ItemStack recipeStack;
      FilterItem filterItemCurrent;
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        stackInSlot = matrix.getStackInSlot(i);
        if (stackInSlot.isEmpty()) {
          recipeStack = recipeCopy.get(i);
          //////////////// booleans are meta, ore(?ignored?), nbt
          filterItemCurrent = !recipeStack.isEmpty() ? new FilterItem(recipeStack, true, false, false) : null;
          //false here means dont simulate
          ItemStack req = tile.request(filterItemCurrent, 1, false);
          matrix.setInventorySlotContents(i, req);
        }
      }
      onCraftMatrixChanged(matrix);
    }
    detectAndSendChanges();
    this.recipeLocked = false;
    //update recipe again in case remnants left : IE hammer and such
    this.onCraftMatrixChanged(this.matrix);
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    if (playerIn.world.isRemote) {
      return ItemStack.EMPTY;
    }

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(slotIndex);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      TileMaster tileMaster = this.getTileMaster();
      if (slotIndex == 0) {
        craftShift(playerIn, tileMaster);
        return ItemStack.EMPTY;
      }
      else if (tileMaster != null) {
        int rest = tileMaster.insertStack(itemstack1, null, false);
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.putStack(stack);
        detectAndSendChanges();
        List<StackWrapper> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, itemstack1);
        return ItemStack.EMPTY;
      }
      if (itemstack1.getCount() == 0) {
        slot.putStack(ItemStack.EMPTY);
      }
      else {
        slot.onSlotChanged();
      }
      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(playerIn, itemstack1);
    }
    return itemstack;
  }

  public final class SlotCraftingNetwork extends SlotCrafting {

    public SlotCraftingNetwork(EntityPlayer player,
        InventoryCrafting craftingInventory, IInventory inventoryIn,
        int slotIndex, int xPosition, int yPosition) {
      super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
    }

    private TileMaster tileMaster;

    @Override
    public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
      if (playerIn.world.isRemote) {
        return stack;
      }
      //        this.onCrafting(stack);
      List<ItemStack> lis = Lists.newArrayList();
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        lis.add(matrix.getStackInSlot(i).copy());
      }
      ///    onCraftMatrixChanged(matrix);
      super.onTake(playerIn, stack);
      detectAndSendChanges();
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        if (matrix.getStackInSlot(i).isEmpty() && getTileMaster() != null) {
          ItemStack req = getTileMaster().request(
              !lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
          if (!req.isEmpty()) {
            matrix.setInventorySlotContents(i, req);
          }
        }
      }
      if (getTileMaster() != null) {
        StorageNetwork.log("remote cancel this stacksMessage");
        //        List<StackWrapper> list = tileMaster.getStacks();
        //        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
      }
      detectAndSendChanges();
      return stack;
    }

    public TileMaster getTileMaster() {
      return tileMaster;
    }

    public void setTileMaster(TileMaster tileMaster) {
      this.tileMaster = tileMaster;
    }
  }
}
