package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.core.utils.NaturalOrderComparator;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.FileUtils;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryShowGUI extends AbstractGUI {

    private final File baseDir;
    private File currentDir;
    private final InventoryScroll scroll;

    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();

    public DirectoryShowGUI(Player p, File baseDir) {
        super(p, 54, Lang.get("directory"), null);
        this.baseDir = baseDir;
        this.currentDir = baseDir;
        scroll = new InventoryScroll(inv, items, InventoryScroll.InventoryScrollType.GAP, 0, 44, 9, false, false);
        setItems();
        p.openInventory(inv);
    }


    @Override
    public void setItems() {
        setItems(currentDir);
    }

    private void setItems(File directory) {
        inv.clear();
        files.clear();
        items.clear();

        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), Lang.get("previous"), Lang.get("previous_desc")));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), Lang.get("next"), Lang.get("next_desc")));


        if (directory == null || !directory.isDirectory()) return;

        List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
        files.sort(new NaturalOrderComparator<>(File::getName));

        for (File f : files) {
            ItemStack item;
            if (f.isDirectory()) {
                item = ItemUtils.createItm(XMaterial.CHEST.parseMaterial(),
                        "§6§l" + f.getName(),
                        Lang.get("file_in_dir", f.listFiles().length),
                        "",
                        Lang.get("click_to_open")
                );
            } else {
                String s = f.getAbsolutePath();
                s = s.substring(s.indexOf("audio"));
                item = ItemUtils.createItm(XMaterial.CHEST.parseMaterial(),
                        "§f§l" + f.getName(),
                        Lang.get("path", s),
                        "",
                        Lang.get("left_click_copy"),
                        Lang.get("right_click_volume"),
                        Lang.get("shift_click_delete")
                );
            }
            items.add(item);
        }
        this.files.addAll(files);
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
                    switch (e.getClick()) {
                        case LEFT:
                            String s = file.getAbsolutePath();
                            s = s.substring(s.indexOf("audio")).replaceAll("\\\\", "/");
                            TextComponent component = new TextComponent(s);
                            component.setUnderlined(true);
                            component.setBold(true);
                            try {
                                component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, s));
                            } catch (Throwable ignored) {
                            }
                            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                    Lang.get("click_copy_clipboard")).create()));
                            owner.spigot().sendMessage(component);
                            break;
                        case RIGHT:
                            new ChatEditor(BoostedAudioSpigot.getInstance(), owner, value -> {
                                try {
                                    float v = Float.parseFloat(value);
                                    try {
                                        File outputFile = new File(file.getParentFile(), "Harmonized_" + file.getName());
                                        if (outputFile.exists()) outputFile.delete();
                                        FileUtils.adjustGain(file.getAbsolutePath(),
                                                outputFile.getAbsolutePath(),
                                                v);
                                        owner.sendMessage(Lang.get("harmonized_message", file.getName(), v));
                                    } catch (Exception exxx) {
                                        if (FileUtils.ffmpeg == null || FileUtils.ffprobe == null) {
                                            owner.sendMessage(Lang.get("ffmpeg_message"));
                                        } else exxx.printStackTrace();
                                    }
                                } catch (Exception ex) {
                                    owner.sendMessage(Lang.get("wrong_values"));
                                }
                            }, Lang.get("enter_chat_gain_adjustment"));
                            break;
                    }
                }
        }
    }

    @Override
    public void onShiftClickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        int index = scroll.getListIndexFromSlot(e.getSlot());
        if (index == -1) return;
        File file = files.get(index);
        file.delete();
        setItems(currentDir);
    }

    @Override
    public void onClose(Player p) {
        if (currentDir.equals(baseDir)) return;
        currentDir = currentDir.getParentFile();
        setItems(currentDir);
        BoostedAudioSpigot.getInstance().getScheduler().runAtEntityLater(p, task -> {
            initSelfListener();
            p.openInventory(inv);
        }, 1);
    }

}