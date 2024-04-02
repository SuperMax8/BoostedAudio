package fr.supermax_8.boostedaudio.spigot.hooks.holograms;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class HologramType<T> {

	protected T hologram;
	protected Location lc;
//	protected boolean defaultVisible;
	protected JavaPlugin instance;

	public HologramType(JavaPlugin instance/*, Location lc, boolean defaultVisible*/) {
		//		this.hologram = hologram;
		this.instance = instance;
		//		this.lc = lc;
		//		this.defaultVisible = defaultVisible;
	}

	public T getT() {
		return hologram;
	}

	public JavaPlugin getInstance() {
		return instance;
	}

	//	public T createHologram(JavaPlugin plugin, Location loc) {
	//		
	//	}

	public abstract boolean supportPerPlayerVisibility();

	public abstract boolean supportItems();

	public abstract T createHologram(Location lc, boolean defaultVisible);

	public void setPlayersVisible(List<Player> players) {
		players.forEach(p -> setPlayerVisibility(p, true));
	}

	public void setPlayerVisibility(Player p, boolean visible) {
		throw new UnsupportedOperationException();
	}

	public void appendItemLine(ItemStack item) {
		throw new UnsupportedOperationException();
	}

	public abstract void appendTextLine(String text);

	public abstract void teleport(Location lc);
	
	public abstract void hide(Player p);
	public abstract void show(Player p);
	
	public abstract boolean isDeleted();
	
	public abstract int size();
	
	public abstract void setline(int i, String txt);

	public abstract void delete();
	
	public abstract void interact(Consumer<Player> interact);
}
