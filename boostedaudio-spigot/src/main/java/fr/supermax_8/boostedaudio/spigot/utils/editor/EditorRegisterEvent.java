package fr.supermax_8.boostedaudio.spigot.utils.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class EditorRegisterEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private Object obj;

    public EditorRegisterEvent(Player p, Object obj) {
        super(p);
        this.obj = obj;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Object getObj() {
        return obj;
    }
}