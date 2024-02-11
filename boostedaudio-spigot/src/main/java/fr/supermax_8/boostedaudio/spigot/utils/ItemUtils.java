package fr.supermax_8.boostedaudio.spigot.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
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

    public static ItemStack createItm(XMaterial mat, String name, String... lore) {
        return createItm(mat.parseMaterial() == null ? Material.DEAD_BUSH : mat.parseMaterial(), name, lore);
    }

    public static void sendHandPacket(final Player player, final ItemStack mapiiitem) {
        int slot = XMaterial.getVersion() > 8 ? 45 : player.getInventory().getHeldItemSlot();
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(XMaterial.getVersion() >= 19 ? 2 : 1, slot);
        packet.getItemModifier().write(0, mapiiitem);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}