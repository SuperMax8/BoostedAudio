package fr.supermax_8.boostedaudio.core.utils;

import com.google.gson.annotations.Expose;

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
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", world='" + world + '\'' +
                '}';
    }

}