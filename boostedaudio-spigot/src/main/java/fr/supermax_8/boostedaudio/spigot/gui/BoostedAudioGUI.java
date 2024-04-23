package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.manager.SpeakerManager;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

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
        inv.setItem(3, ItemUtils.createItm(XMaterial.JUKEBOX.parseMaterial(), Lang.get("playlist_list"), Lang.get("playlist_desc")));
        inv.setItem(5, ItemUtils.createItm(XMaterial.NOTE_BLOCK.parseMaterial(), Lang.get("speakers"),
                Lang.get("speaker_desc"),
                "",
                Lang.get("leftclick_to_open_in_the_world_you_are"),
                Lang.get("rightclick_to_open_the_world_selection")
        ));
        inv.setItem(7, ItemUtils.createItm(XMaterial.MAP.parseMaterial(), Lang.get("regions"),
                Lang.get("region_desc"),
                "",
                Lang.get("leftclick_to_open_in_the_world_you_are"),
                Lang.get("rightclick_to_open_the_world_selection")
        ));
    }

    @Override
    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        owner.closeInventory();
        switch (e.getSlot()) {
            case 1:
                new DirectoryShowGUI(owner, new File(BoostedAudioSpigot.getInstance().getDataFolder(), "webhost" + File.separator + "audio"));
                break;
            case 3:
                new PlayListListGUI(owner, new File(BoostedAudioSpigot.getInstance().getDataFolder(), "data" + File.separator + "playlist"));
                break;
            case 5:
                if (e.getClick().isLeftClick()) {
                    new SpeakerListGUI(owner, owner.getWorld());
                } else
                    new WorldSelectionGUI(owner, (lore, world) -> {
                        SpeakerManager speakerManager = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager();

                        long speakerInWorld = speakerManager.getSpeakers().entrySet().stream()
                                .filter(entry -> entry.getKey().getWorld().equals(world)).count();
                        lore.add("§7Nb speaker in world: §8" + speakerInWorld);
                    }, w -> {
                        owner.closeInventory();
                        new SpeakerListGUI(owner, w);
                    });
                break;
            case 7:
                if (e.getClick().isLeftClick()) {
                    if (BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager() != null)
                        new RegionListGUI(owner, owner.getWorld());
                    else owner.sendMessage(Lang.get("worldguard_error"));
                } else
                    new WorldSelectionGUI(owner, (lore, world) -> {
                        RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();

                        long regionInWorld = regionManager.getAudioRegions().entrySet().stream().filter(
                                entry -> RegionManager.getApi().getRegion(world, entry.getKey()).isPresent()).count();
                        lore.add("§7Nb region in world: §8" + regionInWorld);
                    }, w -> {
                        owner.closeInventory();
                        if (BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager() != null)
                            new RegionListGUI(owner, w);
                        else owner.sendMessage(Lang.get("worldguard_error"));
                    });
                break;
        }
    }


}