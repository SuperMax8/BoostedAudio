package fr.supermax_8.boostedaudio.spigot.hooks.holograms;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class DHologram extends HologramType<Hologram> {

	//	private HologramManager hm;

	public DHologram(JavaPlugin instance/*, Location lc, boolean defaultVisible*/) {
		super(instance/*, lc, defaultVisible*/);
		//		System.out.println("DecentHolograms Hooked");
		//		hm = DecentHologramsAPI.get().getHologramManager();
	}

	@Override
	public void appendTextLine(String text) {
		//		hologram.
		DHAPI.addHologramLine(hologram, text);
	}

	@Override
	public void appendItemLine(ItemStack item) {
		DHAPI.addHologramLine(hologram, item);
	}


	@Override
	public void teleport(Location lc) {
		DHAPI.moveHologram(hologram, lc.add(0, 1, 0));
	}

	@Override
	public void delete() {
		DHAPI.removeHologram(hologram.getName());
		hologram = null;
	}

	@Override
	public Hologram createHologram(Location lc, boolean defaultVisible) {
		hologram = DHAPI.createHologram(UUID.randomUUID().toString(), lc, false);
		//		hologram.hideAll();
		hologram.setDefaultVisibleState(false);
		hologram.enable();
		return hologram;
	}

	@Override
	public void setline(int i, String txt) {
		DHAPI.setHologramLine(hologram, i, txt);
	}

	@Override
	public void setPlayersVisible(List<Player> players) {
		hologram.getPages().forEach(page -> players.forEach(p -> {
			hologram.removeShowPlayer(p);
			hologram.hide(p);
			hologram.setShowPlayer(p);
			hologram.show(p, page.getIndex());
		}));
	}

	@Override
	public void setPlayerVisibility(Player p, boolean visible) {
		if (visible) {
			hologram.setShowPlayer(p);
//			hologram.getPages().forEach(page -> hologram.show(p, page.getIndex()));
			hologram.show(p, 0);
			return;
		}
//		else {
			hologram.removeShowPlayer(p);
			hologram.hide(p);	
//		}
	}

	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}

	@Override
	public boolean supportItems() {
		return false;
	}

	@Override
	public void hide(Player p) {
		hologram.removeShowPlayer(p);
		hologram.hide(p);
		//		hologram.getPages().forEach(page -> hologram.hide(p));
	}

	@Override
	public void show(Player p) {
		hologram.setShowPlayer(p);
//		hologram.getPages().forEach(page -> hologram.show(p, page.getIndex()));
		hologram.show(p, 0);
	}

	@Override
	public boolean isDeleted() {
		return hologram == null;
	}

	@Override
	public int size() {
		return hologram.getPage(0).getLines().size() + 1;
	}

	//	public DHologram() {
	//		 hm = DecentHologramsAPI.get().getHologramManager();
	//	}
	//	
	//	@Override
	//	public Hologram createHologram(JavaPlugin plugin, Location loc) {
	//		return hm.registerHologram(new Hologram(UUID.randomUUID().toString(), loc));
	//	}
}
