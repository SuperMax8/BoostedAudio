package fr.supermax_8.boostedaudio.gui;

import fr.supermax_8.boostedaudio.BoostedAudioLoader;
import fr.supermax_8.boostedaudio.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;

public class DirectoryShowGUI extends AbstractGUI {

    private final File baseDir;
    private File currentDir;
    private final InventoryScroll scroll;

    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();

    public DirectoryShowGUI(Player p, File baseDir) {
        super(p, 54, "§lDirectory", null);
        this.baseDir = baseDir;
        this.currentDir = baseDir;
        scroll = new InventoryScroll(inv, items, InventoryScroll.InventoryScrollType.GAP, 0, 44, 9, false, false);
        setItems();
        p.openInventory(inv);
    }


    @Override
    public void setItems() {
        setItems(baseDir);
    }

    private void setItems(File directory) {
        inv.clear();
        files.clear();
        items.clear();

        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), "§lPrevious", "§7Click to show back"));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), "§lNext", "§7Click to show next"));


        if (directory == null || !directory.isDirectory()) return;
        for (File f : directory.listFiles()) {
            files.add(f);
            ItemStack item;
            if (f.isDirectory()) {
                item = ItemUtils.createItm(XMaterial.CHEST.parseMaterial(),
                        "§6§l" + f.getName(),
                        "§8File in directory: " + f.listFiles().length,
                        "",
                        "§7Click to open"
                );
            } else {
                String s = f.getAbsolutePath();
                s = s.substring(s.indexOf("audio"));
                item = ItemUtils.createItm(XMaterial.CHEST.parseMaterial(),
                        "§f§l" + f.getName(),
                        "§7Path: " + s,
                        "",
                        "§7Left click to copy path"
                );
            }
            items.add(item);
        }
        scroll.setItems();
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        switch (e.getSlot()) {
            case 52:
                scroll.nextClick();
                break;
            case 53:
                scroll.previousClick();
                break;
            default:
                int index = scroll.getListIndexFromSlot(e.getSlot());
                if (index == -1) return;
                File file = files.get(index);
                if (file.isDirectory()) {
                    setItems(file);
                    currentDir = file;
                } else {
                    owner.closeInventory();
                    String s = file.getAbsolutePath();
                    s = s.substring(s.indexOf("audio"));
                    TextComponent component = new TextComponent(s);
                    component.setUnderlined(true);
                    component.setColor(ChatColor.BOLD);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, s));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "Click to copy to clipboard").create()));
                    owner.spigot().sendMessage(component);
                }
        }
    }

    @Override
    public void onClose(Player p) {
        if (currentDir.equals(baseDir)) return;
        currentDir = currentDir.getParentFile();
        setItems(currentDir);
        new BukkitRunnable() {
            @Override
            public void run() {
                initSelfListener();
                p.openInventory(inv);
            }
        }.runTaskLater(BoostedAudioLoader.getInstance(), 1);
    }

}