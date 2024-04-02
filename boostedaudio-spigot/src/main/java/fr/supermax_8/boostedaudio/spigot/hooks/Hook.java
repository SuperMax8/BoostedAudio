package fr.supermax_8.boostedaudio.spigot.hooks;

public enum Hook {
	WORLDGUARD("WorldGuard"),
	HOLOGRAPHICDISPLAYS("HolographicDisplays"),
	PROTOCOLLIB("ProtocolLib"),
	LIBSDISGUISES("LibsDisguises"),
	DECENTHOLOGRAMS("DecentHolograms"),
	PLACEHOLDER_API("PlaceholderAPI")
	;

	private boolean e = false;

	private String plugin, version = null;

	Hook(String plugin) {
		this.plugin = plugin;
	}

	public boolean isEnabled() {
		return e;
	}

	public void enable() {
		e = true;
	}

	public void disable() {
		e = false;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return plugin;
	}
}
