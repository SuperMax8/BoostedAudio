package fr.supermax_8.boostedaudio.spigot.utils;

import fr.supermax_8.boostedaudio.core.utils.SerializableLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class InternalUtils {


    public static Location serializableLocToBukkitLocation(SerializableLocation location) {
        return new Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ(), location.getYaw(), 0);
    }

    public static SerializableLocation bukkitLocationToSerializableLoc(Location location) {
        float convertedYaw = (location.getYaw() + 180) % 360;
        if (convertedYaw < 0) convertedYaw += 360;
        return new SerializableLocation((float) location.getX(), (float) location.getY(), (float) location.getZ(), convertedYaw, location.getWorld().getName());
    }


}