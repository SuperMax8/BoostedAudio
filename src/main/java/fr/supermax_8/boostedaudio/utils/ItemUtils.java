package fr.supermax_8.boostedaudio.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemUtils {

    public static ItemStack createItm(Material mat, String name, String... lore) {
        ItemStack itm = new ItemStack(mat);
        ItemMeta meta = itm.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.addItemFlags(ItemFlag.values());
        itm.setItemMeta(meta);
        return itm;
    }

}