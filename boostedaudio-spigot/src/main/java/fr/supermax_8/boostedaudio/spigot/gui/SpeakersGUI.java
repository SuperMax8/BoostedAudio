package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.SpeakerManager;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SpeakersGUI extends AbstractGUI {

    private final SpeakerManager speakerManager = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager();
    private final InventoryScroll scroll;
    private final List<ItemStack> items;
    private List<Audio> speakersOfWorld;
    private final World world;

    public SpeakersGUI(Player player, World world) {
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
                Lang.get("link", audio.getLinks()),
                Lang.get("location", audio.getSpatialInfo().getLocation()),
                Lang.get("maxdistance", audio.getSpatialInfo().getMaxVoiceDistance()),
                Lang.get("distancemodel", audio.getSpatialInfo().getDistanceModel()),
                Lang.get("refdistance", audio.getSpatialInfo().getRefDistance()),
                Lang.get("rollofffactor", audio.getSpatialInfo().getRolloffFactor()),
                Lang.get("loop", audio.isLoop()),
                "",
                Lang.get("left_click_edit"),
                Lang.get("right_click_copy_params"),
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
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    try {
                        List<String> links = new ArrayList<>();
                        String[] output = s.split(";");
                        Audio.AudioSpatialInfo info;
                        boolean loop = true;
                        double maxVoiceDistance;

                        if (output.length >= 7) {
                            maxVoiceDistance = Double.parseDouble(output[0]);
                            String distanceModel = output[1];
                            double refDistance = Double.parseDouble(output[2]);
                            double rolloffFactor = Double.parseDouble(output[3]);
                            loop = Boolean.parseBoolean(output[4]);
                            info = new Audio.AudioSpatialInfo(
                                    InternalUtils.bukkitLocationToSerializableLoc(owner.getLocation()),
                                    maxVoiceDistance,
                                    distanceModel,
                                    refDistance,
                                    rolloffFactor
                            );
                            links.addAll(Arrays.asList(output).subList(5, output.length));
                        } else {
                            links.add(output[0]);
                            maxVoiceDistance = Double.parseDouble(output[1]);

                            if (output.length == 2) {
                                info = new Audio.AudioSpatialInfo(
                                        InternalUtils.bukkitLocationToSerializableLoc(owner.getLocation()), maxVoiceDistance);
                            } else {
                                String distanceModel = output[2];
                                double refDistance = Double.parseDouble(output[3]);
                                double rolloffFactor = Double.parseDouble(output[4]);
                                loop = Boolean.parseBoolean(output[5]);
                                info = new Audio.AudioSpatialInfo(
                                        InternalUtils.bukkitLocationToSerializableLoc(owner.getLocation()),
                                        maxVoiceDistance,
                                        distanceModel,
                                        refDistance,
                                        rolloffFactor
                                );
                            }
                        }

                        speakerManager.addSpeaker(new Audio(links, info, UUID.randomUUID(), 100, 100, loop));
                        owner.sendMessage(Lang.get("speaker_added"));
                        BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                    } catch (Exception e) {
                        owner.sendMessage(Lang.get("wrong_values"));
                    }
                }, Lang.get("speaker_create"));
                break;
            default:
                int index = scroll.getListIndexFromSlot(slot);
                if (index == -1) return;
                Audio selectedSpeaker = speakersOfWorld.get(index);
                if (event.isRightClick()) {
                    owner.closeInventory();

                    TextComponent component = new TextComponent(Lang.get("copy_clipboard"));

                    component.setUnderlined(true);
                    component.setColor(ChatColor.BOLD);

                    StringJoiner joiner = new StringJoiner(";");
                    selectedSpeaker.getLinks().forEach(joiner::add);
                    String currentParams =
                            selectedSpeaker.getSpatialInfo().getMaxVoiceDistance() + ";" +
                                    selectedSpeaker.getSpatialInfo().getDistanceModel() + ";" +
                                    selectedSpeaker.getSpatialInfo().getRefDistance() + ";" +
                                    selectedSpeaker.getSpatialInfo().getRolloffFactor() + ";" +
                                    selectedSpeaker.isLoop() + ";"
                                    + joiner;

                    try {
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, currentParams));
                    } catch (Throwable e) {

                    }
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            Lang.get("click_copy_clipboard")).create()));

                    owner.spigot().sendMessage(component);
                    return;
                } else if (event.isLeftClick()) {
                    TextComponent component = new TextComponent(Lang.get("speaker_modification"));

                    component.setUnderlined(true);
                    component.setColor(ChatColor.GOLD);

                    StringJoiner joiner = new StringJoiner(";");
                    selectedSpeaker.getLinks().forEach(joiner::add);
                    String currentParams =
                            selectedSpeaker.getSpatialInfo().getMaxVoiceDistance() + ";" +
                                    selectedSpeaker.getSpatialInfo().getDistanceModel() + ";" +
                                    selectedSpeaker.getSpatialInfo().getRefDistance() + ";" +
                                    selectedSpeaker.getSpatialInfo().getRolloffFactor() + ";" +
                                    selectedSpeaker.isLoop() + ";"
                                    + joiner;
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentParams));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            Lang.get("click_to_paste_chat")).create()));

                    new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                        try {
                            String[] output = s.split(";");
                            Audio.AudioSpatialInfo info;
                            boolean loop;
                            double maxVoiceDistance;
                            maxVoiceDistance = Double.parseDouble(output[0]);
                            String distanceModel = output[1];
                            double refDistance = Double.parseDouble(output[2]);
                            double rolloffFactor = Double.parseDouble(output[3]);
                            loop = Boolean.parseBoolean(output[4]);
                            info = new Audio.AudioSpatialInfo(
                                    InternalUtils.bukkitLocationToSerializableLoc(owner.getLocation()),
                                    maxVoiceDistance,
                                    distanceModel,
                                    refDistance,
                                    rolloffFactor
                            );
                            List<String> links = new ArrayList<>(Arrays.asList(output).subList(5, output.length));

                            speakerManager.removeSpeaker(InternalUtils.serializableLocToBukkitLocation(selectedSpeaker.getSpatialInfo().getLocation()));
                            speakerManager.addSpeaker(new Audio(links, info, UUID.randomUUID(), 200, 200, loop));
                            owner.sendMessage(Lang.get("speaker_modified"));
                            BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                        } catch (Exception e) {
                            owner.sendMessage(Lang.get("wrong_values"));
                        }
                    }, component);
                }
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
                speakerManager.removeSpeaker(InternalUtils.serializableLocToBukkitLocation(selectedSpeaker.getSpatialInfo().getLocation()));
                setItems();
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }

}