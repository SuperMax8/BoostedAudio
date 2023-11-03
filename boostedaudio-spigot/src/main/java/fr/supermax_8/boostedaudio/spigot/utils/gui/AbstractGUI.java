package fr.supermax_8.boostedaudio.spigot.utils.gui;

import fr.supermax_8.boostedaudio.core.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.TemporaryListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.wildfly.common.annotation.NotNull;

public abstract class AbstractGUI implements InventoryHolder {

    protected AbstractGUI parent;
    protected Inventory inv;
    protected Player owner;

    public AbstractGUI() {

    }

    public AbstractGUI(AbstractGUI parent) {
        this.parent = parent;
    }

    public AbstractGUI(Player owner, int size, String name, AbstractGUI parent) {
        this(parent);
        this.owner = owner;
        inv = Bukkit.createInventory(this, size, name);
        initSelfListener();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    public abstract void setItems();

    public abstract void onClose(Player p);

    public void onDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public void onDropInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public void onShiftClickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public void onAirClickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    protected void onPlayerInvShiftClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public void onPlayerInvClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public Player getOwner() {
        return owner;
    }

    protected void initSelfListener() {
        TemporaryListener<InventoryClickEvent> click = new TemporaryListener<>(InventoryClickEvent.class, EventPriority.NORMAL, e -> {
            Player p = (Player) e.getWhoClicked();
            if (!p.equals(owner)) return;
            Inventory pinv = p.getInventory();
            Inventory invlick = e.getClickedInventory();
            ItemStack item = e.getCurrentItem();

            switch (e.getClick()) {
                case NUMBER_KEY:
                case DOUBLE_CLICK:
//                case SWAP_OFFHAND:
                    e.setCancelled(true);
                    return;
                // DROP IN CUSTOMINV
                case DROP:
                case CONTROL_DROP:
                    onDropInCustomInv(e);
                    return;
            }
            //CLICK IN PLAYER INVENTORY WHEN CUSTOM INV IS OPEN
            if (pinv.equals(invlick)) {
                if (e.isShiftClick())
                    onPlayerInvShiftClick(e);
                else
                    onPlayerInvClick(e);
                return;
            }
            if ((item == null || item.getType().equals(Material.AIR)) && !pinv.equals(invlick)) {
                onAirClickInCustomInv(e);
                return;
            }
            if (e.isShiftClick()) {
                //CLICK IN THE CUSTOM INV IF HE SHIFT CLICK
                onShiftClickInCustomInv(e);
                return;
            }
            //CLICK IN THE CUSTOM INV
            clickInCustomInv(e);
        });

        TemporaryListener<InventoryDragEvent> drag = new TemporaryListener<>(InventoryDragEvent.class, EventPriority.NORMAL, e -> {
            Player p = (Player) e.getWhoClicked();
            if (!p.equals(owner)) return;
            for (Integer i : e.getRawSlots()) {
                if (i < e.getInventory().getSize()) {
                    onDrag(e);
                    break;
                }
            }
        });

        TemporaryListener<InventoryCloseEvent> close = new TemporaryListener<>(InventoryCloseEvent.class, EventPriority.NORMAL, e -> {
            Player p = (Player) e.getPlayer();
            if (!p.equals(owner)) return false;
            click.unregister();
            drag.unregister();
            onClose(p);
            if (parent != null)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.getOpenInventory().getType().equals(InventoryType.CHEST)) return;
                        parent.setItems();
                        p.openInventory(parent.inv);
                        parent.initSelfListener();
                    }
                }.runTaskLater(BoostedAudioSpigot.getInstance(), 1);
            return true;
        });
    }


}
