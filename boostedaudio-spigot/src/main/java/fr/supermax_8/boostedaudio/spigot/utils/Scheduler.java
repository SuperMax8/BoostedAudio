package fr.supermax_8.boostedaudio.spigot.utils;

import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class Scheduler {

    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final BoostedAudioSpigot main = BoostedAudioSpigot.getInstance();


    public static void runTask(Consumer<BukkitTask> run) {
        scheduler.runTask(main, run);
    }

    public static BukkitTask runTask(Runnable run) {
        return scheduler.runTask(main, run);
    }

    public static BukkitTask runTaskTimer(Runnable run, long delay, long period) {
        return scheduler.runTaskTimer(main, run, delay, period);
    }

    public static void runTaskTimer(Consumer<BukkitTask> run, long delay, long period) {
        scheduler.runTaskTimer(main, run, delay, period);
    }

    public static void runTaskLater(Consumer<BukkitTask> run, long delay) {
        scheduler.runTaskLater(main, run, delay);
    }

    public static BukkitTask runTaskLater(Runnable run, long delay) {
        return scheduler.runTaskLater(main, run, delay);
    }

    public static void runTaskAsync(Runnable run) {
        scheduler.runTaskAsynchronously(main, run);
    }

    public static void runTaskAsync(Consumer<BukkitTask> run) {
        scheduler.runTaskAsynchronously(main, run);
    }


    public static void runTaskTimerAsync(Runnable run, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(main, run, delay, period);
    }

    public static void runTaskTimerAsync(Consumer<BukkitTask> run, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(main, run, delay, period);
    }

    public static BukkitTask runTaskLaterAsync(Runnable run, long delay) {
        return scheduler.runTaskLaterAsynchronously(main, run, delay);
    }

    public static void runTaskLaterAsync(Consumer<BukkitTask> run, long delay) {
        scheduler.runTaskLaterAsynchronously(main, run, delay);
    }

    private static String s = "%%__USER__%%";

}