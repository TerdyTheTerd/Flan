package com.flemmli97.flan.gui;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public abstract class ServerOnlyScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final List<ScreenHandlerListener> listeners = Lists.newArrayList();

    protected ServerOnlyScreenHandler(int syncId, PlayerInventory playerInventory, int rows, Object... additionalData) {
        super(fromRows(rows), syncId);
        int i = (rows - 4) * 18;
        this.inventory = new SimpleInventory(rows * 9);
        this.fillInventoryWith(playerInventory.player, this.inventory, additionalData);
        int n;
        int m;
        for (n = 0; n < rows; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(this.inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
            }
        }

        for (n = 0; n < 3; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
            }
        }

        for (n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
        }
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> fromRows(int rows) {
        switch (rows) {
            case 2:
                return ScreenHandlerType.GENERIC_9X2;
            case 3:
                return ScreenHandlerType.GENERIC_9X3;
            case 4:
                return ScreenHandlerType.GENERIC_9X4;
            case 5:
                return ScreenHandlerType.GENERIC_9X5;
            case 6:
                return ScreenHandlerType.GENERIC_9X6;
        }
        return ScreenHandlerType.GENERIC_9X1;
    }

    protected abstract void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData);

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i < 0)
            return ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (this.isRightSlot(i))
            this.handleSlotClicked((ServerPlayerEntity) playerEntity, i, slot, j);
        this.sendContentUpdates();
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (!(player instanceof ServerPlayerEntity))
            return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (this.isRightSlot(index))
            this.handleSlotClicked((ServerPlayerEntity) player, index, slot, 0);
        this.sendContentUpdates();
        return ItemStack.EMPTY;
    }

    @Override
    public void addListener(ScreenHandlerListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            listener.onHandlerRegistered(this, this.getStacks());
            this.sendContentUpdates();
        }
    }

    @Override
    public void sendContentUpdates() {
        int j;
        for (j = 0; j < this.slots.size(); ++j) {
            ItemStack itemStack = this.slots.get(j).getStack();

            for (ScreenHandlerListener screenHandlerListener : this.listeners) {
                screenHandlerListener.onSlotUpdate(this, j, itemStack.copy());
            }
        }
    }

    protected abstract boolean isRightSlot(int slot);

    /**
     * @param clickType 0 for left click, 1 for right click
     */
    protected abstract boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType);
}
