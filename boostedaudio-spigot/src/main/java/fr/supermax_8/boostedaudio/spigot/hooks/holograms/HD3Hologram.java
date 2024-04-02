package fr.supermax_8.boostedaudio.spigot.hooks.holograms;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings.Visibility;
import me.filoghost.holographicdisplays.api.hologram.line.ClickableHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickEvent;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickListener;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;



public class HD3Hologram extends HologramType<Hologram> {

	public HD3Hologram(JavaPlugin instance/*, Location lc, boolean defaultVisible*/) {
		super(instance/*, lc, defaultVisible*/);
		//		System.out.println("Holographic Displays Hooked");
		//		instance.getServer().getConsoleSender().sendMessage("§f[" + instance.getDescription().getName() + "]Holographic Displays 3.X Hooked");
		//		hologram = HologramsAPI.createHologram(instance, lc);
	}

	@Override
	public void appendTextLine(String text) {
		hologram.getLines().appendText(text);
	}

	@Override
	public void teleport(Location lc) {
		hologram.setPosition(lc);
	}

	@Override
	public void delete() {
		hologram.delete();
//				hologram = null;
	}

	@Override
	public Hologram createHologram(Location lc, boolean defaultVisible) {
		hologram = HolographicDisplaysAPI.get(instance).createHologram(lc);
		if (!defaultVisible) hologram.getVisibilitySettings().setGlobalVisibility(Visibility.HIDDEN);
		else hologram.getVisibilitySettings().setGlobalVisibility(Visibility.VISIBLE);
		return hologram;
	}

	@Override
	public void setline(int i, String txt) {
		((TextHologramLine)hologram.getLines().get(i)).setText(txt);
	}

	@Override
	public void appendItemLine(ItemStack item) {
		hologram.getLines().appendItem(item);
	}

	@Override
	public void setPlayersVisible(List<Player> players) {
		players.forEach(p -> {
			hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.HIDDEN);
			hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.VISIBLE);
		});
	}

	@Override
	public void setPlayerVisibility(Player p, boolean visible) {
		if (visible) hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.VISIBLE);
		else hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.HIDDEN);
	}

	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}

	@Override
	public boolean supportItems() {
		return true;
	}

	@Override
	public boolean isDeleted() {
		return hologram.isDeleted();
	}

	@Override
	public int size() {
		return hologram.getLines().size();
	}

	@Override
	public void hide(Player p) {
		hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.HIDDEN);
	}

	@Override
	public void show(Player p) {
		hologram.getVisibilitySettings().setIndividualVisibility(p, Visibility.VISIBLE);
	}

	@Override
	public void interact(Consumer<Player> interact) {
		for (int i = 0; i < hologram.getLines().size(); i++) {
			ClickableHologramLine line = (ClickableHologramLine) hologram.getLines().get(i);
			line.setClickListener(e -> interact.accept(e.getPlayer()));
		}
	}

}
