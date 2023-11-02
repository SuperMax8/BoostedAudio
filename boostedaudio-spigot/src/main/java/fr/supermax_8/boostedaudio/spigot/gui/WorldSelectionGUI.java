package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WorldSelectionGUI extends AbstractGUI {

    private final InventoryScroll scroll;
    private final List<World> worldList;
    private final List<ItemStack> items;
    private final Consumer<World> onClick;

    public WorldSelectionGUI(Player player, Consumer<World> onClick) {
        super(player, 54, "§lChoose World", null);
        this.items = new ArrayList<>();
        this.onClick = onClick;
        this.worldList = Bukkit.getWorlds();
        this.scroll = new InventoryScroll(getInventory(), items, InventoryScroll.InventoryScrollType.GAP, 0, 44, 9, false, false);
        setItems();
        player.openInventory(getInventory());
    }

    @Override
    public void setItems() {
        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), "§lPrevious", "§7Click to show back"));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), "§lNext", "§7Click to show next"));

        for (World world : worldList) {
            ItemStack item = ItemUtils.createItm(XMaterial.MAP.parseMaterial(), "§6§l" + world.getName());
            items.add(item);
        }
        scroll.setItems();
    }

    @Override
    public void clickInCustomInv(InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();
        switch (slot) {
            case 52:
                scroll.previousClick();
                break;
            case 53:
                scroll.nextClick();
                break;
            default:
        }
        int index = scroll.getListIndexFromSlot(slot);

        if (index == -1) return;
        World selectedWorld = worldList.get(index);
        onClick.accept(selectedWorld);
    }

    @Override
    public void onClose(Player player) {
    }


}
