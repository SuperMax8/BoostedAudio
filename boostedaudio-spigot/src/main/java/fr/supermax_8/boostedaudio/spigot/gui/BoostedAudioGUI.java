package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class BoostedAudioGUI extends AbstractGUI {

    public BoostedAudioGUI(Player p) {
        super(p, 9, "BoostedAudio", null);
        setItems();
        p.openInventory(inv);
    }

    @Override
    public void setItems() {
        inv.setItem(1, ItemUtils.createItm(XMaterial.MUSIC_DISC_CAT.parseMaterial(), Lang.get("audio"),
                Lang.get("audio_desc")));
        inv.setItem(4, ItemUtils.createItm(XMaterial.JUKEBOX.parseMaterial(), Lang.get("speakers"),
                Lang.get("speaker_desc")));
        inv.setItem(7, ItemUtils.createItm(XMaterial.MAP.parseMaterial(), Lang.get("regions"),
                Lang.get("region_desc")));
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        switch (e.getSlot()) {
            case 1:
                owner.closeInventory();
                new DirectoryShowGUI(owner, new File(BoostedAudioSpigot.getInstance().getDataFolder(), "webhost" + File.separator + "audio"));
                break;
            case 4:
                owner.closeInventory();
                new WorldSelectionGUI(owner, w -> {
                    owner.closeInventory();
                    new SpeakersGUI(owner, w);
                });
                break;
            case 7:
                owner.closeInventory();
                new WorldSelectionGUI(owner, w -> {
                    owner.closeInventory();
                    if (BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager() != null) new RegionsGUI(owner, w);
                    else owner.sendMessage(Lang.get("worldguard_error"));
                });
                break;
        }
    }


    @Override
    public void onClose(Player p) {

    }


}