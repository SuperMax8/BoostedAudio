package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.api.audio.PlayList;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.SpeakerManager;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SpeakerListGUI extends AbstractGUI {

    private final SpeakerManager speakerManager = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager();
    private final InventoryScroll scroll;
    private final List<ItemStack> items;
    private List<Audio> speakersOfWorld;
    private final World world;

    public SpeakerListGUI(Player player, World world) {
        super(player, 54, Lang.get("speakers_title", world.getName()), null);
        this.items = new ArrayList<>();
        this.world = world;
        this.scroll = new InventoryScroll(getInventory(), items, InventoryScroll.InventoryScrollType.GAP, 0, 44, 9, false, false);
        setItems();
        player.openInventory(getInventory());
    }

    @Override
    public void setItems() {
        inv.clear();
        items.clear();

        Map<Location, Audio> speakers = speakerManager.getSpeakers();
        speakersOfWorld = speakers.entrySet().stream().filter(entry -> entry.getKey().getWorld().equals(world)).map(Map.Entry::getValue).collect(Collectors.toList());

        for (Audio audio : speakersOfWorld) {
            ItemStack item = createSpeakerItem(audio);
            items.add(item);
        }
        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), Lang.get("add_speaker"), Lang.get("add_speaker_desc")));
        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), Lang.get("previous"), Lang.get("previous_desc")));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), Lang.get("next"), Lang.get("next_desc")));

        scroll.setItems();
    }

    private ItemStack createSpeakerItem(Audio audio) {
        return ItemUtils.createItm(XMaterial.JUKEBOX.parseMaterial(), "§l" + audio.getId(),
                audio.getPlayList().getId() != null ? Lang.get("link_or_playlist", audio.getPlayList().getId()) : Lang.get("link_or_playlist", audio.getPlayList().getLinks()),
                Lang.get("location", audio.getSpatialInfo().getLocation()),
                Lang.get("maxdistance", audio.getSpatialInfo().getMaxVoiceDistance()),
                Lang.get("distancemodel", audio.getSpatialInfo().getDistanceModel()),
                Lang.get("refdistance", audio.getSpatialInfo().getRefDistance()),
                Lang.get("rollofffactor", audio.getSpatialInfo().getRolloffFactor()),
                Lang.get("loop", audio.isLoop()),
                Lang.get("synchronous", audio.isSynchronous()),
                "",
                Lang.get("left_click_edit"),
                Lang.get("shift_right_to_remove")
        );
    }

    @Override
    public void clickInCustomInv(InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();

        switch (slot) {
            case 52:
                scroll.nextClick();
                break;
            case 53:
                scroll.previousClick();
                break;
            case 45:
                owner.closeInventory();
                BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
                    new SpeakerEditGUI(owner, this);
                });
                break;
            default:
                int index = scroll.getListIndexFromSlot(slot);
                if (index == -1) return;
                Audio selectedSpeaker = speakersOfWorld.get(index);

                StringJoiner linksJoiner = new StringJoiner(";");
                String playlistId = selectedSpeaker.getPlayList().getId();
                if (playlistId == null)
                    for (String s : selectedSpeaker.getPlayList().getLinks()) linksJoiner.add(s);
                else linksJoiner.add(playlistId);

                owner.closeInventory();
                BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
                    new SpeakerEditGUI(owner, this,
                            linksJoiner.toString(),
                            selectedSpeaker.getFadeIn(),
                            selectedSpeaker.getFadeOut(),
                            selectedSpeaker.isLoop(),
                            selectedSpeaker.isSynchronous(),
                            selectedSpeaker.getSpatialInfo().getMaxVoiceDistance(),
                            selectedSpeaker.getSpatialInfo().getRefDistance(),
                            selectedSpeaker.getSpatialInfo().getRolloffFactor(),
                            selectedSpeaker.getSpatialInfo().getDistanceModel(),
                            selectedSpeaker.getSpatialInfo().getLocation()
                    );
                });
                break;
        }
    }

    public void onShiftClickInCustomInv(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        int index = scroll.getListIndexFromSlot(slot);
        if (index == -1) return;

        if (index < speakersOfWorld.size()) {
            Audio selectedSpeaker = speakersOfWorld.get(index);

            if (event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                speakerManager.removeSpeaker(InternalUtils.serializableLocToBukkitLocation(selectedSpeaker.getSpatialInfo().getLocation()), true);
                //BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                setItems();
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }

}