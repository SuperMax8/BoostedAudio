package fr.supermax_8.boostedaudio.spigot.utils.gui;

import fr.supermax_8.boostedaudio.core.utils.MathUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InventoryScroll {

    private final Inventory inv;
    private List<ItemStack> items;
    private final InventoryScrollType type;

    private final int start;
    private final int end;

    private int rowPostionMin;
    private int rowPostionMax;

    private int gap;
    private int finalGap;

    private int displaySize;

    private boolean cuboidSelection;
    private boolean infiniteLoop;
    private final HashMap<Integer, ItemStack> itemSelected = new HashMap<>();
    private ItemStack selectedAllItem = null;

    public enum InventoryScrollType {
        PAGE,
        GAP
    }

    public InventoryScroll(Inventory inv, List<ItemStack> items, InventoryScrollType type, int start, int end, int finalGap, boolean cuboidSelection, boolean infiniteLoop) {
        this.inv = inv;
        this.items = items;
        this.type = type;
        this.start = start;
        this.end = end;
        switch (type) {
            case GAP:
                gap = 0;
                break;
            case PAGE:
                gap = 1;
                break;
        }
        this.finalGap = finalGap;
        this.cuboidSelection = cuboidSelection;
        this.infiniteLoop = infiniteLoop;
        if (cuboidSelection) {
            rowPostionMin = MathUtils.getInventoryCollumn(start);
            rowPostionMax = MathUtils.getInventoryCollumn(end);
        }
        displaySize = 0;
        for (int slot = start; slot <= end; slot++) {
            if (checkSkip(slot)) continue;
            if (displaySize >= items.size() - 1) break;
            displaySize++;
        }

    }

    public InventoryScroll(Inventory inv, List<ItemStack> items, InventoryScrollType type, int start, int end) {
        this.inv = inv;
        this.items = items;
        this.type = type;
        this.start = start;
        this.end = end;
        gap = 1;
    }

    public InventoryScroll(Inventory inv, ItemStack[] items, InventoryScrollType type, int start, int end) {
        this(inv, Arrays.asList(items), type, start, end);
    }

    public InventoryScroll(Inventory inv, InventoryScrollType type, int start, int end, ItemStack... items) {
        this(inv, Arrays.asList(items), type, start, end);
    }

    public void setItems() {
        switch (type) {
            case PAGE:
                //Here "move" = page
                final int gap = end - start + 1;
                int count = start;
                for (int i = gap * (this.gap - 1); i <= gap * this.gap; i++) {
                    if (count == end + 1) break;
                    if (items.size() <= i) {
                        inv.setItem(count, null);
                        count++;
                        continue;
                    }
                    ItemStack selected = itemSelected.get(count);
                    if (selected != null) inv.setItem(count, selected);
                    else inv.setItem(count, items.get(i));
                    count++;
                }
                break;
            case GAP:
                // The starting counter from gap
                int indexGap = this.gap;

                for (int slot = start; slot <= end; slot++) {
                    if (checkSkip(slot)) continue;
                    if (indexGap >= items.size()) {
                        if (infiniteLoop) {
                            indexGap = 0;
                        } else {
                            inv.setItem(slot, null);
                            continue;
                        }
                    }
                    ItemStack selected = selectedAllItem;
                    if (selected == null) selected = itemSelected.get(indexGap);

                    if (selected != null) inv.setItem(slot, selected);
                    else inv.setItem(slot, indexGap > items.size() - 1 ? null : items.get(indexGap));
                    indexGap++;
                }
                break;
        }
    }

    private boolean checkSkip(int slot) {
        if (!cuboidSelection) return false;
        int slotRow = MathUtils.getInventoryCollumn(slot);
        return !(slotRow >= rowPostionMin && slotRow <= rowPostionMax);
    }

    private boolean inSelection(int slot) {
        return !checkSkip(slot) && (slot >= start && slot <= end);
    }

    public int getListIndexFromSlot(int slot) {
        if (!inSelection(slot)) return -1;
        switch (type) {
            case GAP:
                int indexGap = gap;
                for (int i = start; i < slot; i++) {
                    if (checkSkip(i)) continue;
                    if (indexGap >= items.size() - 1)
                        indexGap = 0;
                    else
                        indexGap++;
                }
                return indexGap;
            case PAGE:
                return (slot - start) * gap;
        }
        return -1;
    }

    public void setItemList(ArrayList<ItemStack> list) {
        items = list;
    }

    public void selectAll(ItemStack itm) {
        selectedAllItem = itm;
    }

    public void unselectAll() {
        selectedAllItem = null;
    }

    public void unselectAllSingle() {
        itemSelected.clear();
    }

    public int selectFromSlot(int slot, ItemStack itm) {
        int index = getListIndexFromSlot(slot);
        selectFromIndex(index, itm);
        return index;
    }

    public void selectFromIndex(int index, ItemStack itm) {
        itemSelected.put(index, itm);
    }

    public ItemStack unselectFromSlot(int slot) {
        return unselectFromIndex(getListIndexFromSlot(slot));
    }

    public ItemStack unselectFromIndex(int index) {
        return itemSelected.remove(index);
    }


    public void nextClick() {
        switch (type) {
            case PAGE:
                final int gap = end - start/* + 1*/;
                if (items.size() < this.gap * gap) {
                    if (infiniteLoop) this.gap = 1;
                    return;
                }
                this.gap++;
                break;
            case GAP:
                int newIndexGap = this.gap + finalGap;
//                System.out.println("---");
//                System.out.println("newIndexGap: " + newIndexGap);
//                System.out.println("displaySize: " + displaySize);
//                System.out.println("items.size(): " + items.size());
//                System.out.println("---");
                if (infiniteLoop) {
                    if (newIndexGap > items.size() - 1) newIndexGap = 0;
                } else {
                    if (newIndexGap + displaySize - finalGap > items.size() - 1) newIndexGap = 0;
                    if (newIndexGap > items.size() - 1) return;
                }
                this.gap = newIndexGap;
                break;
        }
        setItems();
    }


    public void previousClick() {
        switch (type) {
            case PAGE:
                if (gap == 1) return;
                gap--;
                break;
            case GAP:
                int newIndexGap = gap - finalGap;
                if (newIndexGap < 0) {
                    if (!infiniteLoop) return;
                    newIndexGap = items.size() - 1;
                }
                if (newIndexGap > items.size() - 1) return;
                gap = newIndexGap;
                break;
        }
        setItems();
    }


    public HashMap<Integer, ItemStack> getItemSelected() {
        return itemSelected;
    }

}