package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.editor.ChatEditor;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class RegionEditGUI extends AbstractGUI {

    private final RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();

    private String region = "", links = "";
    private int fadeIn = 300, fadeOut = 300;
    private boolean loop = true, synchronous = true;
    private final AbstractGUI lastGui;
    private boolean edit = false;

    public RegionEditGUI(Player player, AbstractGUI lastGui, String region, String links, int fadeIn, int fadeOut, boolean loop, boolean synchronous) {
        this(player, lastGui);
        this.region = region;
        this.links = links;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
        this.synchronous = synchronous;
        setItems();
    }

    public RegionEditGUI(Player player, AbstractGUI lastGui) {
        super(player, 18, "§6Edit Region", null);
        this.lastGui = lastGui;
        setItems();
        player.openInventory(getInventory());
    }

    @Override
    public void setItems() {
        edit = false;
        inv.setItem(0, ItemUtils.createItm(XMaterial.MAP, Lang.get("region", region)));

        String[] linkss = links.split(";");
        for (int i = 0; i < linkss.length; i++) {
            linkss[i] = "§7- §f" + linkss[i];
        }
        inv.setItem(1, ItemUtils.createItm(
                        XMaterial.NOTE_BLOCK,
                        Lang.get("link", ""),
                        linkss
                )
        );
        inv.setItem(2, ItemUtils.createItm(XMaterial.DISPENSER, Lang.get("fadein", fadeIn)));
        inv.setItem(3, ItemUtils.createItm(XMaterial.DROPPER, Lang.get("fadeout", fadeOut)));
        inv.setItem(4, ItemUtils.createItm(XMaterial.REPEATING_COMMAND_BLOCK, Lang.get("loop", loop)));
        inv.setItem(5, ItemUtils.createItm(XMaterial.CLOCK, Lang.get("synchronous", synchronous)));

        inv.setItem(14, ItemUtils.createItm(XMaterial.EMERALD, Lang.get("confirm_edition")));
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        switch (e.getSlot()) {
            case 0 -> {
                edit = true;
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    if (RegionManager.getApi().getRegion(owner.getWorld(), s).isPresent())
                        region = s;
                    setItems();
                }, Lang.get("enter_region"));
            }
            case 1 -> {
                edit = true;
                SpeakerEditGUI.sendLinksMessage(links, owner.spigot());
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    initSelfListener();
                    owner.openInventory(inv);
                    links = s;
                    setItems();
                }, Lang.get("enter_links"));
            }
            case 2 -> {
                switch (e.getClick()) {
                    case LEFT -> fadeIn += 50;
                    case RIGHT -> fadeIn = Math.max(0, fadeIn - 50);
                    case MIDDLE, DROP, CONTROL_DROP -> fadeIn = 0;
                }
                setItems();
            }
            case 3 -> {
                switch (e.getClick()) {
                    case LEFT -> fadeOut += 50;
                    case RIGHT -> fadeOut = Math.max(0, fadeOut - 50);
                    case MIDDLE, DROP, CONTROL_DROP -> fadeOut = 0;
                }
                setItems();
            }
            case 4 -> {
                loop = !loop;
                setItems();
            }
            case 5 -> {
                synchronous = !synchronous;
                setItems();
            }
            case 14 -> owner.closeInventory();
        }
    }


    @Override
    public void onClose(Player p) {
        if (edit) return;
        if (!region.isEmpty()) {
            ArrayList<String> linkss = new ArrayList<>(Arrays.asList(links.split(";")));
            regionManager.addRegion(region, new Audio(linkss, null, UUID.randomUUID(), fadeIn, fadeOut, loop, synchronous));
            BoostedAudioSpigot.getInstance().getAudioManager().saveData();
        }
        BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
            lastGui.initSelfListener();
            lastGui.setItems();
            p.openInventory(lastGui.getInventory());
        });
    }


}