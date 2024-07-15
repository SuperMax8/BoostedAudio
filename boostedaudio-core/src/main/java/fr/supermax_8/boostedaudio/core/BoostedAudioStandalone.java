package fr.supermax_8.boostedaudio.core;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.multiserv.BoostedAudioProxy;
import fr.supermax_8.boostedaudio.core.utils.ColorUtils;
import fr.supermax_8.boostedaudio.core.utils.MojangAPI;

import java.io.File;
import java.util.Scanner;
import java.util.UUID;

public class BoostedAudioStandalone {

    private BoostedAudioProxy proxy;

    public BoostedAudioStandalone(File dataFolder) {
        BoostedAudioAPIImpl.sendMessage = s -> {
            System.out.println(ColorUtils.translateColorCodes("§8§l[§9§lBoostedAudio§8§l] §7" + s));
            System.out.print("> ");
        };
        BoostedAudioAPI.getAPI().info("Starting BoostedAudio: " + (Limiter.isPremium() ? "&6&lPremium" : "&2Free"));

        BoostedAudioAPIImpl.internalAPI = new InternalAPI() {
            @Override
            public String getUsername(UUID uuid) {
                try {
                    return MojangAPI.getUsernameFromUUID(uuid.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return " ";
                }
            }
        };

        proxy = new BoostedAudioProxy(dataFolder, BaPluginVersion.getVersion());
        BoostedAudioAPI.getAPI().info("StandAlone BoostedAudio Started!!!");
        initConsoleInput();
    }

    private void initConsoleInput() {
        Scanner scanner = new Scanner(System.in);

        print("Welcome to the console app. Type 'help' for a list of commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("stop")) {
                print("Exiting the app. Goodbye!");
                System.exit(0);
                break;
            }

            switch (input) {
                case "help" -> {
                    print(
                            "Commands:",
                            "  help",
                            "  stop"
                    );
                }
                default -> {
                    print("Unknown command: " + input + " - Type 'help' for a list of commands.");
                }
            }
        }

        scanner.close();
    }

    private void print(String... messages) {
        for (String s : messages) System.out.println(s);
    }

}