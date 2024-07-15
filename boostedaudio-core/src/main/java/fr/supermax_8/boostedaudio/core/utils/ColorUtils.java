package fr.supermax_8.boostedaudio.core.utils;

public class ColorUtils {

    // ANSI characters

    // Colors
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String DARK_BLUE = "\u001B[34m";
    public static final String DARK_GREEN = "\u001B[32m";
    public static final String DARK_AQUA = "\u001B[36m";
    public static final String DARK_RED = "\u001B[31m";
    public static final String DARK_PURPLE = "\u001B[35m";
    public static final String GOLD = "\u001B[33m";
    public static final String GRAY = "\u001B[37m";
    public static final String DARK_GRAY = "\u001B[90m";
    public static final String BLUE = "\u001B[94m";
    public static final String GREEN = "\u001B[92m";
    public static final String AQUA = "\u001B[96m";
    public static final String RED = "\u001B[91m";
    public static final String LIGHT_PURPLE = "\u001B[95m";
    public static final String YELLOW = "\u001B[93m";
    public static final String WHITE = "\u001B[97m";

    // Styles
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String STRIKETHROUGH = "\u001B[9m";


    public static String translateColorCodes(String text) {
        return text
                .replace("&0", BLACK).replace("§0", BLACK)
                .replace("&1", DARK_BLUE).replace("§1", DARK_BLUE)
                .replace("&2", DARK_GREEN).replace("§2", DARK_GREEN)
                .replace("&3", DARK_AQUA).replace("§3", DARK_AQUA)
                .replace("&4", DARK_RED).replace("§4", DARK_RED)
                .replace("&5", DARK_PURPLE).replace("§5", DARK_PURPLE)
                .replace("&6", GOLD).replace("§6", GOLD)
                .replace("&7", GRAY).replace("§7", GRAY)
                .replace("&8", DARK_GRAY).replace("§8", DARK_GRAY)
                .replace("&9", BLUE).replace("§9", BLUE)
                .replace("&a", GREEN).replace("§a", GREEN)
                .replace("&b", AQUA).replace("§b", AQUA)
                .replace("&c", RED).replace("§c", RED)
                .replace("&d", LIGHT_PURPLE).replace("§d", LIGHT_PURPLE)
                .replace("&e", YELLOW).replace("§e", YELLOW)
                .replace("&f", WHITE).replace("§f", WHITE)
                .replace("&l", BOLD).replace("§l", BOLD)
                .replace("&o", ITALIC).replace("§o", ITALIC)
                .replace("&n", UNDERLINE).replace("§n", UNDERLINE)
                .replace("&m", STRIKETHROUGH).replace("§m", STRIKETHROUGH)
                .replace("&r", RESET).replace("§r", RESET);
    }

}