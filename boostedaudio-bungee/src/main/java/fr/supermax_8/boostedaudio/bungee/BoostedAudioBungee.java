package fr.supermax_8.boostedaudio.bungee;

import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfiguration;
import fr.supermax_8.boostedaudio.bungee.utils.BungeeCrossConfigurationSection;
import fr.supermax_8.boostedaudio.core.BoostedAudioConfiguration;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.utils.configuration.ConfigUpdater;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfiguration;
import fr.supermax_8.boostedaudio.core.utils.configuration.CrossConfigurationSection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class BoostedAudioBungee extends Plugin implements Listener {

    private BoostedAudioHost host;
    private BoostedAudioConfiguration configuration;

    @Override
    public void onEnable() {
        CrossConfiguration.instancer = BungeeCrossConfiguration::new;
        CrossConfigurationSection.converter = o -> new BungeeCrossConfigurationSection((Configuration) o);

        loadConf();
        host = new BoostedAudioHost(configuration);

        ProxyServer.getInstance().registerChannel("boostedaudio:fromproxy");
        ProxyServer.getInstance().registerChannel("boostedaudio:fromspigot");


        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            System.out.println("Broadcasting message to all servers");
            ProxyServer.getInstance().getServersCopy().forEach((s, info) -> {
                info.sendData("boostedaudio:fromproxy", "From bungee messaaaaaage !!!!!!!".getBytes());
            });
        }, 0, 5, TimeUnit.SECONDS);
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }

    private void loadConf() {
        getDataFolder().mkdirs();
        File f = new File(getDataFolder(), "config.yml");
        if (!f.exists()) try (InputStream in = getResourceAsStream("config.yml")) {
            Files.copy(in, f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ConfigUpdater.update(this::getResourceAsStream, "config.yml", new File(getDataFolder(), "config.yml"));
        } catch (Exception ignored) {
        }
        configuration = new BoostedAudioConfiguration(f);
    }


    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!e.getTag().equals("boostedaudio:fromspigot")) return;

        //System.out.println("Received message from " + e.getSender() + " : " + new String(e.getData()) + " on channel " + e.getTag());
    }


}