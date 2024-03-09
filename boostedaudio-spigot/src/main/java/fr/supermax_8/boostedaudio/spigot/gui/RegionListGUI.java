package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.audio.Audio;
import fr.supermax_8.boostedaudio.core.utils.Lang;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import fr.supermax_8.boostedaudio.spigot.utils.XMaterial;
import fr.supermax_8.boostedaudio.spigot.utils.gui.AbstractGUI;
import fr.supermax_8.boostedaudio.spigot.utils.gui.InventoryScroll;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RegionListGUI extends AbstractGUI {

    private final RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();
    private final InventoryScroll scroll;
    private final List<ItemStack> items;
    private LinkedHashMap<String, Audio> regionsOfWorld;
    private final World world;

    public RegionListGUI(Player player, World world) {
        super(player, 54, Lang.get("regions_title", world.getName()), null);
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

        Map<String, Audio> regions = regionManager.getAudioRegions();
        regionsOfWorld = new LinkedHashMap<>(
                regions.entrySet().stream().filter(
                        entry -> RegionManager.getApi().getRegion(world, entry.getKey()).isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        for (Map.Entry<String, Audio> entry : regionsOfWorld.entrySet()) {
            String region = entry.getKey();
            Audio audio = entry.getValue();
            ItemStack item = createRegionItem(region, audio);
            items.add(item);
        }

        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), Lang.get("add_region"), Lang.get("add_region_desc")));
        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), Lang.get("previous"), Lang.get("previous_desc")));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), Lang.get("next"), Lang.get("next_desc")));

        scroll.setItems();
    }

    private ItemStack createRegionItem(String region, Audio audio) {
        return ItemUtils.createItm(XMaterial.MAP.parseMaterial(), "§l" + region,
                Lang.get("link", audio.getLinks()),
                Lang.get("region", region),
                Lang.get("fadein", audio.getFadeIn()),
                Lang.get("fadeout", audio.getFadeOut()),
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
                    new RegionEditGUI(owner,this);
                });
                break;
            default:
                int index = scroll.getListIndexFromSlot(slot);
                if (index == -1) return;
                Map.Entry<String, Audio> selectedRegion = (Map.Entry<String, Audio>) regionsOfWorld.entrySet().toArray()[index];

                Audio audio = selectedRegion.getValue();
                StringJoiner linksJoiner = new StringJoiner(";");
                for (String s : audio.getLinks()) linksJoiner.add(s);
                owner.closeInventory();
                BoostedAudioSpigot.getInstance().getScheduler().runNextTick(t -> {
                    new RegionEditGUI(owner, this, selectedRegion.getKey(), linksJoiner.toString(), audio.getFadeIn(), audio.getFadeOut(), audio.isLoop(), audio.isSynchronous());
                });
                break;
        }
    }

    public void onShiftClickInCustomInv(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        int index = scroll.getListIndexFromSlot(slot);
        if (index == -1) return;

        if (index < regionsOfWorld.size()) {
            Map.Entry<String, Audio> selectedRegion = (Map.Entry<String, Audio>) regionsOfWorld.entrySet().toArray()[index];

            if (event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                regionManager.removeRegion(selectedRegion.getKey());
                setItems();
                BoostedAudioSpigot.getInstance().getAudioManager().saveData();
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }

}