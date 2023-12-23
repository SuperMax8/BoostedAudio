package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.user.Audio;
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
        super(player, 54, "§lSpeakers of world " + world.getName(), null);
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
        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), "§2§lAdd speaker", "§7Add a new speaker at your location"));
        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), "§lPrevious", "§7Click to show back"));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), "§lNext", "§7Click to show next"));

        scroll.setItems();
    }

    private ItemStack createSpeakerItem(Audio audio) {
        return ItemUtils.createItm(XMaterial.JUKEBOX.parseMaterial(), "§l" + audio.getId(),
                "§7Links: " + audio.getLinks(),
                "§7Location: " + audio.getSpatialInfo().getLocation(),
                "§7Max distance: " + audio.getSpatialInfo().getMaxVoiceDistance(),
                "§7Distance model: " + audio.getSpatialInfo().getDistanceModel(),
                "§7Ref distance: " + audio.getSpatialInfo().getRefDistance(),
                "§7Rolloff factor: " + audio.getSpatialInfo().getRolloffFactor(),
                "§7Loop: " + audio.isLoop(),
                "",
                "§7LeftClick to edit",
                "§7RightClick to copy params",
                "§c§lShift RightClick to remove"
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
                        owner.sendMessage("§aSpeaker added");
                        BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                    } catch (Exception e) {
                        owner.sendMessage("§cWrong values, read the format and try again");
                    }
                }, "§6Enter the values of the new speaker in the chat, it will be placed at your position",
                        "§7Format:",
                        "§7link;maxVoiceDistance;distanceModel(optional);refDistance(optional);rolloffFactor(optional);loop(optional)",
                        "OR",
                        "§7maxVoiceDistance;distanceModel;refDistance;rolloffFactor;loop;links1;links2(optional);links3(optional);links4(optional)"
                );
                break;
            default:
                int index = scroll.getListIndexFromSlot(slot);
                if (index == -1) return;
                Audio selectedSpeaker = speakersOfWorld.get(index);
                if (event.isRightClick()) {
                    owner.closeInventory();

                    TextComponent component = new TextComponent("Copy to clipboard");

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
                            "Click to copy to clipboard").create()));

                    owner.spigot().sendMessage(component);
                    return;
                } else if (event.isLeftClick()) {
                    TextComponent component = new TextComponent("Enter the values for the modification of the speaker in the chat " +
                            "§7Format: §7maxVoiceDistance;distanceModel;refDistance;rolloffFactor;loop;links1;links2(optional);links3(optional);links4(optional)");

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
                            "Click to copy to chat").create()));

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
                            owner.sendMessage("§aSpeaker modified");
                            BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                        } catch (Exception e) {
                            owner.sendMessage("§cWrong values, read the format and try again");
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
