package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.user.Audio;
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
    private int fadeIn, fadeOut;
    private boolean loop, syncronous;

    public RegionEditGUI(Player player, AbstractGUI lastGui, String region, String links, int fadeIn, int fadeOut, boolean loop, boolean syncronous) {
        this(player, lastGui);
        this.region = region;
        this.links = links;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.loop = loop;
        this.syncronous = syncronous;
        setItems();
    }

    public RegionEditGUI(Player player, AbstractGUI lastGui) {
        super(player, 18, "§6Edit Region", lastGui);
        setItems();
        player.openInventory(getInventory());
    }

    @Override
    public void setItems() {
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
        inv.setItem(5, ItemUtils.createItm(XMaterial.CLOCK, Lang.get("syncronous", syncronous)));

        inv.setItem(14, ItemUtils.createItm(XMaterial.EMERALD, Lang.get("confirm_edition")));
    }

    public void clickInCustomInv(InventoryClickEvent e) {
        e.setCancelled(true);
        switch (e.getSlot()) {
            case 0 -> {
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                    owner.openInventory(inv);
                    region = s;
                    setItems();
                }, Lang.get("enter_region"));
            }
            case 1 -> {
                new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
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
            }
            case 3 -> {
                switch (e.getClick()) {
                    case LEFT -> fadeOut += 50;
                    case RIGHT -> fadeOut = Math.max(0, fadeOut - 50);
                    case MIDDLE, DROP, CONTROL_DROP -> fadeOut = 0;
                }
            }
            case 4 -> loop = !loop;
            case 5 -> syncronous = !syncronous;
            case 14 -> {
                owner.closeInventory();
            }
        }
        setItems();
    }


    @Override
    public void onClose(Player p) {
        ArrayList<String> linkss = new ArrayList<>(Arrays.asList(links.split(";")));
        regionManager.addRegion(region, new Audio(linkss, null, UUID.randomUUID(), fadeIn, fadeOut, loop, syncronous));
        BoostedAudioSpigot.getInstance().getAudioManager().saveData();
    }


}