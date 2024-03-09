package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.SpeakerManager;
import fr.supermax_8.boostedaudio.spigot.utils.InternalUtils;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class SpeakerEditGUI extends AbstractGUI {

    private final SpeakerManager speakerManager = BoostedAudioSpigot.getInstance().getAudioManager().getSpeakerManager();

    private String links = "", distanceModel = "";
    private int fadeIn = 300, fadeOut = 300;
    private boolean loop = true, synchronous = false;
    private double maxVoiceDistance, refFactor, refDistance;
    private SerializableLocation location;
    private final AbstractGUI lastGui;
    private boolean edit = false;

    public SpeakerEditGUI(Player player, AbstractGUI lastGui, String links, int fadeIn, int fadeOut, boolean loop, boolean synchronous, double maxVoiceDistance, double refDistance, double refFactor, String distanceModel, SerializableLocation location) {
        this(player, lastGui);
        this.links = links;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
        this.synchronous = synchronous;
        this.maxVoiceDistance = maxVoiceDistance;
        this.refDistance = refDistance;
        this.refFactor = refFactor;
        this.distanceModel = distanceModel;
        this.location = location;
        setItems();
    }

    public SpeakerEditGUI(Player player, AbstractGUI lastGui) {
        super(player, 18, "§6Edit Speaker", null);
        this.lastGui = lastGui;
        setItems();
        player.openInventory(getInventory());
    }

    @Override
    public void setItems() {
        edit = false;
        String[] linkss = links.split(";");
        for (int i = 0; i < linkss.length; i++) {
            linkss[i] = "§7- §f" + linkss[i];
        }
        inv.setItem(0, ItemUtils.createItm(
                        XMaterial.NOTE_BLOCK,
                        Lang.get("link", ""),
                        linkss
                )
        );
        inv.setItem(1, ItemUtils.createItm(XMaterial.DISPENSER, Lang.get("fadein", fadeIn)));
        inv.setItem(2, ItemUtils.createItm(XMaterial.DROPPER, Lang.get("fadeout", fadeOut)));
        inv.setItem(3, ItemUtils.createItm(XMaterial.REPEATING_COMMAND_BLOCK, Lang.get("loop", loop)));
        inv.setItem(4, ItemUtils.createItm(XMaterial.CLOCK, Lang.get("synchronous", synchronous)));

        inv.setItem(9, ItemUtils.createItm(XMaterial.LEAD, Lang.get("maxdistance", maxVoiceDistance)));

        if (maxVoiceDistance > 0) {
            inv.setItem(10, ItemUtils.createItm(XMaterial.PAPER, Lang.get("distancemodel", distanceModel)));
            inv.setItem(11, ItemUtils.createItm(XMaterial.TRIPWIRE_HOOK, Lang.get("refdistance", refDistance)));
            inv.setItem(12, ItemUtils.createItm(XMaterial.TRIPWIRE_HOOK, Lang.get("rollofffactor", refFactor)));
        }

        inv.setItem(14, ItemUtils.createItm(XMaterial.EMERALD, Lang.get("confirm_edition")));
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        switch (e.getSlot()) {
            case 0 -> {
                edit = true;
                sendLinksMessage(links, owner.spigot());

                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    links = s;
                    setItems();
                }, Lang.get("enter_links"));
            }
            case 9 -> {
                edit = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    try {
                        maxVoiceDistance = Double.parseDouble(s);
                        if (maxVoiceDistance <= 0) return;
                        if (refDistance == 0) {
                            Audio.AudioSpatialInfo calc = new Audio.AudioSpatialInfo(null, maxVoiceDistance);
                            refDistance = calc.getRefDistance();
                            refFactor = calc.getRolloffFactor();
                            distanceModel = calc.getDistanceModel();
                        }
                        setItems();
                    } catch (Exception ex) {

                    }
                }, Lang.get("enter_maxVoiceDistance"));
            }
            case 10 -> {
                edit = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    try {
                        s = s.toLowerCase();
                        switch (s) {
                            case "linear", "exponential", "inverse" -> {
                            }
                            default -> {
                                return;
                            }
                        }
                        distanceModel = s;
                        setItems();
                    } catch (Exception ignored) {
                    }
                }, Lang.get("enter_distanceModel"));
            }
            case 11 -> {
                edit = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    try {
                        double d = Double.parseDouble(s);
                        if (d <= 0) return;
                        refDistance = d;
                        setItems();
                    } catch (Exception ex) {

                    }
                }, Lang.get("enter_RefDistance"));
            }
            case 12 -> {
                edit = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    try {
                        double d = Double.parseDouble(s);
                        if (d <= 0) return;
                        refFactor = d;
                        setItems();
                    } catch (Exception ex) {

                    }
                }, Lang.get("enter_RefFactor"));
            }
            case 1 -> {
                switch (e.getClick()) {
                    case LEFT -> fadeIn += 50;
                    case RIGHT -> fadeIn = Math.max(0, fadeIn - 50);
                    case MIDDLE, DROP, CONTROL_DROP -> fadeIn = 0;
                }
                setItems();
            }
            case 2 -> {
                switch (e.getClick()) {
                    case LEFT -> fadeOut += 50;
                    case RIGHT -> fadeOut = Math.max(0, fadeOut - 50);
                    case MIDDLE, DROP, CONTROL_DROP -> fadeOut = 0;
                }
                setItems();
            }
            case 3 -> {
                loop = !loop;
                setItems();
            }
            case 4 -> {
                synchronous = !synchronous;
                setItems();
            }
            case 14 -> owner.closeInventory();
        }
    }

    static void sendLinksMessage(String links, Player.Spigot spigot) {
        TextComponent component = new TextComponent(Lang.get("links_copy_chat"));
        try {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, links));
        } catch (Throwable ex) {
        }
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                "Click").create()));
        spigot.sendMessage(component);
    }


    @Override
    public void onClose(Player p) {
        if (edit) return;
        ArrayList<String> linkss = new ArrayList<>(Arrays.asList(links.split(";")));
        speakerManager.addSpeaker(new Audio(linkss, new Audio.AudioSpatialInfo(
                location == null ? InternalUtils.bukkitLocationToSerializableLoc(p.getLocation()) : location,
                maxVoiceDistance, distanceModel, refDistance, refFactor),
                UUID.randomUUID(),
                fadeIn,
                fadeOut,
                loop,
                synchronous
        ));
        BoostedAudioSpigot.getInstance().getAudioManager().saveData();
        BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
            lastGui.initSelfListener();
            lastGui.setItems();
            p.openInventory(lastGui.getInventory());
        });
    }


}