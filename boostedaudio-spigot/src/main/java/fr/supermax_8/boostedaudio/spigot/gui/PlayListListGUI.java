package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.audio.PlayList;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.core.utils.NaturalOrderComparator;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlayListListGUI extends AbstractGUI {

    private final File baseDir;
    private File currentDir;
    private final InventoryScroll scroll;
    private boolean closeByPass = false;
    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();

    public PlayListListGUI(Player p, File baseDir) {
        super(p, 54, Lang.get("playlist_list"), null);
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

        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), Lang.get("add_playlist")));
        inv.setItem(46, ItemUtils.createItm(XMaterial.CHEST.parseMaterial(), Lang.get("add_folder")));

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
                        Lang.get("click_to_open"),
                        Lang.get("shift_click_delete")
                );
            } else {
                if (!f.getName().endsWith(".yml")) continue;

                FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
                List<String> lore = new ArrayList<>() {{
                    Optional<String> list = fc.getKeys(false).stream().findFirst();
                    if (list.isPresent())
                        for (String s : fc.getStringList(list.get()))
                            add("§7-" + s);
                    add("");
                    add(Lang.get("click_to_open"));
                    add(Lang.get("shift_click_delete"));
                }};
                item = ItemUtils.createItm(XMaterial.JUKEBOX.parseMaterial(),
                        "§e§l" + f.getName().replace(".yml", ""),
                        lore
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
            case 45:
                // Create playlist file
                closeByPass = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    String name = s.replaceAll("\\.", "").replaceAll(" ", "_").toLowerCase();
                    File newPlayList = new File(currentDir, name + ".yml");
                    try {
                        newPlayList.createNewFile();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    YamlConfiguration fc = YamlConfiguration.loadConfiguration(newPlayList);
                    fc.set(name, List.of(""));

                    PlayList pl = new PlayList(name, List.of(""));
                    BoostedAudioSpigot.getInstance().getAudioManager().getPlayListManager().addPlayList(pl);
                    setItems(currentDir);
                    reOpenGUI();
                }, Lang.get("enter_playlist_name"));
                break;
            case 46:
                closeByPass = true;
                // Create folder
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    String name = s.replaceAll("\\.", "");
                    File newPlayList = new File(currentDir, name);
                    newPlayList.mkdirs();

                    setItems(currentDir);
                    reOpenGUI();
                }, Lang.get("enter_folder_name"));
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
                    new PlayListEditGUI(owner, file, this);
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
        BoostedAudioSpigot.getInstance().getAudioManager().getPlayListManager().remove(file.getName().replace(".yml", ""));
        setItems(currentDir);
    }

    @Override
    public void onClose(Player p) {
        if (closeByPass) {
            closeByPass = false;
            return;
        }
        if (currentDir.equals(baseDir)) return;
        currentDir = currentDir.getParentFile();
        setItems(currentDir);
        BoostedAudioSpigot.getInstance().getScheduler().runAtEntityLater(p, task -> {
            initSelfListener();
            p.openInventory(inv);
        }, 1);
    }

}