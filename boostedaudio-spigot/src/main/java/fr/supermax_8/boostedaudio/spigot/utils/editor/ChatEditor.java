package fr.supermax_8.boostedaudio.spigot.utils.editor;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class ChatEditor implements Listener {

    private final Player p;
    private final Consumer<String> c;
    private final JavaPlugin instance;

    public ChatEditor(JavaPlugin instance, Player p, Consumer<String> c, String... msg) {
        this.instance = instance;
        this.p = p;
        this.c = c;
        p.closeInventory();
        p.sendMessage(msg);
        Bukkit.getPluginManager().registerEvents(this, instance);
        Bukkit.getPluginManager().callEvent(new EditorRegisterEvent(p, this));
    }

    public ChatEditor(JavaPlugin instance, Player p, Consumer<String> c, TextComponent msg) {
        this.instance = instance;
        this.p = p;
        this.c = c;
        p.closeInventory();
        p.spigot().sendMessage(msg);
        Bukkit.getPluginManager().registerEvents(this, instance);
        Bukkit.getPluginManager().callEvent(new EditorRegisterEvent(p, this));
    }

    @EventHandler
    public void onEditorRegister(EditorRegisterEvent e) {
        Player p = e.getPlayer();
        if (!this.p.equals(p)) return;
        if (!this.equals(e.getObj())) HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!p.equals(this.p)) return;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (!p.equals(this.p)) return;

        e.setCancelled(true);

        HandlerList.unregisterAll(this);

        String msg = e.getMessage();

        Bukkit.getScheduler().runTask(instance, () -> c.accept(msg));

    }

}