package fr.supermax_8.boostedaudio.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableLocation {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final String world;

    public SerializableLocation(float x, float y, float z, float yaw, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.world = world;
    }

    public SerializableLocation(org.bukkit.Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();

        float convertedYaw = (location.getYaw() + 180) % 360;
        if (convertedYaw < 0) convertedYaw += 360;

        yaw = convertedYaw;
        world = location.getWorld().getName();
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public String getWorld() {
        return world;
    }

    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, 0);
    }

    @Override
    public String toString() {
        return "x: " + x + ", y: " + y + ", z: " + z;
    }


}