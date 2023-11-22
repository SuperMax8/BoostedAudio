package fr.supermax_8.boostedaudio.spigot.utils;

import com.comphenix.packetwrapper.wrappers.play.clientbound.WrapperPlayServerSetSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    public static void sendHandPacket(final Player player, final ItemStack mapiiitem) {
        final WrapperPlayServerSetSlot slotPacket = new WrapperPlayServerSetSlot();

        if (XMaterial.getVersion() > 8) slotPacket.setSlot(45);
        else slotPacket.setSlot(player.getInventory().getHeldItemSlot());
        slotPacket.setItemStack(mapiiitem);
        slotPacket.sendPacket(player);
    }


}