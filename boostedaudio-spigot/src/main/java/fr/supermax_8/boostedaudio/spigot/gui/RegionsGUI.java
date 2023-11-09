package fr.supermax_8.boostedaudio.spigot.gui;

import fr.supermax_8.boostedaudio.api.user.Audio;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.manager.RegionManager;
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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RegionsGUI extends AbstractGUI {

    private final RegionManager regionManager = BoostedAudioSpigot.getInstance().getAudioManager().getRegionManager();
    private final InventoryScroll scroll;
    private final List<ItemStack> items;
    private LinkedHashMap<String, Audio> regionsOfWorld;
    private final World world;

    public RegionsGUI(Player player, World world) {
        super(player, 54, "§lRegions of world " + world.getName(), null);
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
        regionsOfWorld = new LinkedHashMap<>(regions.entrySet().stream().filter(
                        entry -> RegionManager.getApi().getRegion(world, entry.getKey()).isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        for (Map.Entry<String, Audio> entry : regionsOfWorld.entrySet()) {
            String region = entry.getKey();
            Audio audio = entry.getValue();
            ItemStack item = createRegionItem(region, audio);
            items.add(item);
        }

        inv.setItem(45, ItemUtils.createItm(XMaterial.EMERALD.parseMaterial(), "§2§lAdd region audio", "§7Add a new speaker at your location"));
        inv.setItem(52, ItemUtils.createItm(XMaterial.RED_WOOL.parseMaterial(), "§lPrevious", "§7Click to show back"));
        inv.setItem(53, ItemUtils.createItm(XMaterial.GREEN_WOOL.parseMaterial(), "§lNext", "§7Click to show next"));

        scroll.setItems();
    }

    private ItemStack createRegionItem(String region, Audio audio) {
        return ItemUtils.createItm(XMaterial.MAP.parseMaterial(), "§l" + region,
                "§7Link: " + audio.getLinks(),
                "§7Region: " + region,
                "§7Fade in: " + audio.getFadeIn() + "ms",
                "§7Fade out: " + audio.getFadeOut() + "ms",
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
                        String[] output = s.split(";");
                        String region;
                        int fadeIn = 500;
                        int fadeOut = 500;
                        boolean loop = true;
                        List<String> links = new ArrayList<>();
                        if (output.length > 5) {
                            region = output[0];
                            fadeIn = Integer.parseInt(output[1]);
                            fadeOut = Integer.parseInt(output[2]);
                            loop = Boolean.parseBoolean(output[3]);
                            links.addAll(Arrays.asList(output).subList(4, output.length));
                        } else {
                            links.add(output[0]);
                            region = output[1];
                            if (output.length > 2) {
                                fadeIn = Integer.parseInt(output[2]);
                                fadeOut = Integer.parseInt(output[3]);
                                loop = Boolean.parseBoolean(output[4]);
                            }
                        }
                        regionManager.addRegion(region, new Audio(links, null, UUID.randomUUID(), fadeIn, fadeOut, loop));
                        owner.sendMessage("§aRegion audio added");
                        BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                    } catch (Exception e) {
                        owner.sendMessage("§cWrong values, read the format and try again");
                    }
                }, "§6Enter the values of the new region audio in the chat",
                        "§7Formats:",
                        "§7link;region;fadeIn(optional);fadeOut(optional);loop(optional)",
                        "§7OR",
                        "§7region;fadeIn;fadeOut;loop;link1;link2;link3(optional)..."
                );
                break;
            default:
                int index = scroll.getListIndexFromSlot(slot);
                if (index == -1) return;
                Map.Entry<String, Audio> selectedRegion = (Map.Entry<String, Audio>) regionsOfWorld.entrySet().toArray()[index];
                String params = params(selectedRegion.getKey(), selectedRegion.getValue());
                if (event.isRightClick()) {
                    owner.closeInventory();

                    TextComponent component = new TextComponent("Copy to clipboard");

                    component.setUnderlined(true);
                    component.setColor(ChatColor.BOLD);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, params));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "Click to copy to clipboard").create()));
                    return;
                } else if (event.isLeftClick()) {
                    TextComponent component = new TextComponent("Enter the values for the modification of the speaker in the chat " +
                            "§7Format: region;fadeIn;fadeOut;loop;link1;link2;link3(optional)...");

                    component.setUnderlined(true);
                    component.setColor(ChatColor.GOLD);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, params));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "Click to copy to chat").create()));

                    new ChatEditor(BoostedAudioSpigot.getInstance(), owner, s -> {
                        try {
                            String[] output = s.split(";");
                            String region = output[0];
                            int fadeIn = Integer.parseInt(output[1]);
                            int fadeOut = Integer.parseInt(output[2]);
                            boolean loop = Boolean.parseBoolean(output[3]);
                            List<String> links = new ArrayList<>(Arrays.asList(output).subList(4, output.length));

                            regionManager.addRegion(region, new Audio(links, null, UUID.randomUUID(), fadeIn, fadeOut, loop));
                            owner.sendMessage("§aRegion audio modified");
                            BoostedAudioSpigot.getInstance().getAudioManager().saveData();
                        } catch (Exception e) {
                            owner.sendMessage("§cWrong values, read the format and try again");
                        }
                    }, component);
                }
                break;
        }
    }

    private String params(String region, Audio audio) {
        List<String> params = new ArrayList<>() {{
            add(region);
            add(audio.getFadeIn() + "");
            add(audio.getFadeOut() + "");
            add(audio.isLoop() + "");
            addAll(audio.getLinks());
        }};
        return String.join(";", params);
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
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }

}