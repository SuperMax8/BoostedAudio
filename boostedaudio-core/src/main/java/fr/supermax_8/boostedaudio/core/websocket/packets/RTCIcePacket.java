package fr.supermax_8.boostedaudio.core.websocket.packets;

import com.google.gson.*;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.user.User;
import fr.supermax_8.boostedaudio.core.websocket.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.core.websocket.HostUser;

import java.lang.reflect.Type;
import java.util.UUID;

public class RTCIcePacket implements Packet {
    private static String s = "%%__USER__%%";
    private final String layerId;
    private final String type;
    private final String candidate;

    private final UUID from;
    private final UUID to;

    public RTCIcePacket(String layerId, String type, String candidate, UUID from, UUID to) {
        this.layerId = layerId;
        this.type = type;
        this.candidate = candidate;
        this.from = from;
        this.to = to;
    }

    @Override
    public void onReceive(HostUser user, AudioWebSocketServer server) {
        if (user.getRemotePeers(layerId).contains(to)) {
            User remoteUser = server.manager.getUsers().get(to);
            if (remoteUser != null) {
                remoteUser.sendPacket(this);
            }
        } else {
           /* user.getSession().close();
            BoostedAudio.debug("KickICE");*/
        }
    }

    public static class Adapter implements JsonSerializer<RTCIcePacket>, JsonDeserializer<RTCIcePacket> {


        @Override
        public RTCIcePacket deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String layerId = object.get("layerId").getAsString();
            String typeRTC = object.get("type").getAsString();
            UUID from = UUID.fromString(object.get("from").getAsString());
            UUID to = UUID.fromString(object.get("to").getAsString());
            String candidate = BoostedAudioAPI.api.getGson().toJson(object.get("candidate").getAsJsonObject());
            return new RTCIcePacket(layerId, typeRTC, candidate, from, to);
        }

        @Override
        public JsonElement serialize(RTCIcePacket rtcIcePacket, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            object.addProperty("type", rtcIcePacket.type);
            object.addProperty("layerId", rtcIcePacket.layerId);
            object.addProperty("from", rtcIcePacket.from.toString());
            object.addProperty("to", rtcIcePacket.to.toString());
            object.add("candidate", new JsonParser().parse(rtcIcePacket.candidate).getAsJsonObject());
            return object;
        }


    }

}