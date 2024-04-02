package fr.supermax_8.boostedaudio.spigot.hooks.holograms;

import org.bukkit.plugin.java.JavaPlugin;

import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;

public class Hologram {

	private HologramType<?> holo;

	public Hologram(JavaPlugin instance) {
		if (BoostedAudioSpigot.getInstance().getHologramType() instanceof DHologram) {
			holo = new DHologram(instance);
			return;
		}
		holo = new HD3Hologram(instance);
	}

	public HologramType<?> getHolo() {
		return holo;
	}

}
