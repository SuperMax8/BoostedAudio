package fr.supermax_8.boostedaudio.gui;

import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.ingame.SpeakerManager;
import fr.supermax_8.boostedaudio.utils.*;
import fr.supermax_8.boostedaudio.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.web.Audio;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpeakersGUI extends AbstractGUI {

    private final SpeakerManager speakerManager = BoostedAudio.getInstance().getAudioManager().getSpeakerManager();
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
                "§7Link: " + audio.getLink(),
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
                new ChatEditor(owner, s -> {
                    try {
                        String[] output = s.split(";");
                        String link = output[0];
                        double maxVoiceDistance = Double.parseDouble(output[1]);
                        boolean loop = true;

                        Audio.AudioSpatialInfo info;
                        if (output.length == 2) {
                            info = new Audio.AudioSpatialInfo(
                                    new SerializableLocation(owner.getLocation()), maxVoiceDistance);
                        } else {
                            String distanceModel = output[2];
                            double refDistance = Double.parseDouble(output[3]);
                            double rolloffFactor = Double.parseDouble(output[4]);
                            loop = Boolean.parseBoolean(output[5]);
                            info = new Audio.AudioSpatialInfo(
                                    new SerializableLocation(owner.getLocation()),
                                    maxVoiceDistance,
                                    distanceModel,
                                    refDistance,
                                    rolloffFactor
                            );
                        }

                        speakerManager.addSpeaker(new Audio(link, info, UUID.randomUUID(), 100, 100, loop));
                        owner.sendMessage("§aSpeaker added");
                        BoostedAudio.getInstance().getAudioManager().saveData();
                    } catch (Exception e) {
                        owner.sendMessage("§cWrong values, read the format and try again");
                    }
                }, "§6Enter the values of the new speaker in the chat, it will be placed at your position",
                        "§7Format: link;maxVoiceDistance;distanceModel(optional);refDistance(optional);rolloffFactor(optional);loop(optional)");
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
                    String currentParams = selectedSpeaker.getLink() + ";" +
                            selectedSpeaker.getSpatialInfo().getMaxVoiceDistance() + ";" +
                            selectedSpeaker.getSpatialInfo().getDistanceModel() + ";" +
                            selectedSpeaker.getSpatialInfo().getRefDistance() + ";" +
                            selectedSpeaker.getSpatialInfo().getRolloffFactor() + ";" +
                            selectedSpeaker.isLoop();
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, currentParams));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "Click to copy to clipboard").create()));
                    return;
                }
                if (event.isLeftClick()) {
                    TextComponent component = new TextComponent("Enter the values for the modification of the speaker in the chat " +
                            "§7Format: link;maxVoiceDistance;distanceModel;refDistance;rolloffFactor;loop");

                    component.setUnderlined(true);
                    component.setColor(ChatColor.GOLD);
                    String currentParams = selectedSpeaker.getLink() + ";" +
                            selectedSpeaker.getSpatialInfo().getMaxVoiceDistance() + ";" +
                            selectedSpeaker.getSpatialInfo().getDistanceModel() + ";" +
                            selectedSpeaker.getSpatialInfo().getRefDistance() + ";" +
                            selectedSpeaker.getSpatialInfo().getRolloffFactor() + ";" +
                            selectedSpeaker.isLoop();
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentParams));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "Click to copy to chat").create()));

                    new ChatEditor(owner, s -> {
                        try {
                            String[] output = s.split(";");
                            String link = output[0];
                            double maxVoiceDistance = Double.parseDouble(output[1]);

                            Audio.AudioSpatialInfo info;
                            String distanceModel = output[2];
                            double refDistance = Double.parseDouble(output[3]);
                            double rolloffFactor = Double.parseDouble(output[4]);
                            boolean loop = Boolean.parseBoolean(output[5]);
                            info = new Audio.AudioSpatialInfo(
                                    selectedSpeaker.getSpatialInfo().getLocation(),
                                    maxVoiceDistance,
                                    distanceModel,
                                    refDistance,
                                    rolloffFactor
                            );

                            speakerManager.removeSpeaker(selectedSpeaker.getSpatialInfo().getLocation().toBukkitLocation());
                            speakerManager.addSpeaker(new Audio(link, info, UUID.randomUUID(), 200, 200, loop));
                            owner.sendMessage("§aSpeaker modified");
                            BoostedAudio.getInstance().getAudioManager().saveData();
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
                speakerManager.removeSpeaker(selectedSpeaker.getSpatialInfo().getLocation().toBukkitLocation());
                setItems();
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }

}