package fr.supermax_8.boostedaudio.core.utils;

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
        return "x: " + x + ", y: " + y + ", z: " + z;
    }


}