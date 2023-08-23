package fr.supermax_8.boostedaudio.web.packets;

import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.util.Map;
import java.util.UUID;

public class UpdateVocalPositionsPacket implements Packet {

    private final Location clientLoc;
    private final Map<UUID, Location> playersAround;

    public UpdateVocalPositionsPacket(Location clientLoc, Map<UUID, Location> playersAround) {
        this.clientLoc = clientLoc;
        this.playersAround = playersAround;
    }

    @Override
    public void onReceive(User session, AudioWebSocketServer server) {
        session.getSession().close();
    }


    public static class Location {

        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final String world;

        public Location(float x, float y, float z, float yaw, String world) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.world = world;
        }

        public Location(org.bukkit.Location location) {
            x = location.getX();
            y = location.getY();
            z = location.getZ();

            float convertedYaw = (location.getYaw() + 180) % 360;
            if (convertedYaw < 0) convertedYaw += 360;

            yaw = convertedYaw;
            world = location.getWorld().getName();
        }


    }

}