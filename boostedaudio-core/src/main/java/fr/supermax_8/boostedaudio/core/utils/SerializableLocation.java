package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.annotations.Expose;

import java.awt.desktop.PrintFilesEvent;

public class SerializableLocation {

    @Expose
    private final double x;
    @Expose
    private final double y;
    @Expose
    private final double z;
    @Expose
    private final float yaw;
    @Expose
    private final String world;

    public SerializableLocation(float x, float y, float z, float yaw, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.world = world;
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

    @Override
    public String toString() {
        long aInt = Math.round(x * 1000);
        long bInt = Math.round(y * 1000);
        long cInt = Math.round(z * 1000);
        long dInt = Math.round(yaw * 1000);
        return aInt + ";" + bInt + ";" + cInt + ";" + dInt + ";" + world;
    }

    public static SerializableLocation fromString(String serialized) {
        String[] parts = serialized.split(";");
        return new SerializableLocation(parseBigInt(parts[0]), parseBigInt(parts[1]), parseBigInt(parts[2]), parseBigInt(parts[3]), parts[4]);
    }

    private static float parseBigInt(String s) {
        return Long.parseLong(s) / 1000f;
    }

}