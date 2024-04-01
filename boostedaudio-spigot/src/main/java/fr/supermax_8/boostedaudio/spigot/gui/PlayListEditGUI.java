package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.core.utils.Lang;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayListEditGUI extends AbstractGUI {

    private final File playlistfile;
    private File currentDir;
    private final InventoryScroll scroll;
    private boolean closeByPass = false;
    private final ArrayList<String> songlist = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();

    public PlayListEditGUI(Player p, File playlistfile, AbstractGUI parent) {
        super(p, 54, Lang.get("playlist_edit"), parent);
        this.playlistfile = playlistfile;
        this.currentDir = playlistfile;
        scroll = new InventoryScroll(inv, items, InventoryScroll.InventoryScrollType.GAP, 0, 44, 9, false, false);
        setItems();
        p.openInventory(inv);
    }


    @Override
    public void setItems() {
        setItems(currentDir);
    }

    private void setItems(File playlistfile) {
        inv.clear();
        songlist.clear();
        items.clear();

        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), Lang.get("add_sound")));

        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), Lang.get("previous"), Lang.get("previous_desc")));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), Lang.get("next"), Lang.get("next_desc")));


        if (playlistfile == null || playlistfile.isDirectory()) return;

        FileConfiguration fc = YamlConfiguration.loadConfiguration(playlistfile);
        Optional<String> key = fc.getKeys(false).stream().findFirst();
        if (key.isPresent()) songlist.addAll(fc.getStringList(key.get()));

        for (String song : songlist) {
            items.add(ItemUtils.createItm(
                    XMaterial.MUSIC_DISC_CAT.parseMaterial(),
                    "§6§l" + song
            ));
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
            case 45:
                closeByPass = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    songlist.add(s);
                    refreshFileFromSongList();

                    initSelfListener();
                    owner.openInventory(inv);
                }, Lang.get("enter_sound"));
                break;
            /*default:
                int index = scroll.getListIndexFromSlot(e.getSlot());
                if (index == -1) return;
                String song = songlist.get(index);

                refreshFileFromSongList();*/
        }
    }

    @Override
    public void onShiftClickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        int index = scroll.getListIndexFromSlot(e.getSlot());
        if (index == -1) return;
        songlist.remove(index);
        refreshFileFromSongList();
    }

    private void refreshFileFromSongList() {
        YamlConfiguration fc = YamlConfiguration.loadConfiguration(playlistfile);
        fc.set(playlistfile.getName().replace(".yml", ""), songlist);
        try {
            fc.save(playlistfile);
        } catch (Exception exx) {
            exx.printStackTrace();
        }
        setItems();
        List<String> l = BoostedAudioSpigot.getInstance().getAudioManager().getPlayListManager().get(playlistfile.getName().replace(".yml", "")).getLinks();
        l.clear();
        l.addAll(songlist);
    }

    @Override
    public void onClose(Player p) {
        if (closeByPass) {
            closeByPass = false;
            return;
        }
        super.onClose(p);
    }


}