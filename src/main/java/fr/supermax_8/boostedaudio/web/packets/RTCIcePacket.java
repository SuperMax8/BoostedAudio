package fr.supermax_8.boostedaudio.web.packets;

import com.google.gson.*;
import fr.supermax_8.boostedaudio.BoostedAudio;
import fr.supermax_8.boostedaudio.web.AudioWebSocketServer;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;

import java.lang.reflect.Type;
import java.util.UUID;

public class RTCIcePacket implements Packet {
    private final String type;
    private final String candidate;

    private final UUID from;
    private final UUID to;

    public RTCIcePacket(String type, String candidate, UUID from, UUID to) {
        this.type = type;
        this.candidate = candidate;
        this.from = from;
        this.to = to;
    }

    @Override
    public void onReceive(User user, AudioWebSocketServer server) {
        if (user.getRemotePeers().contains(to)) {
            User remoteUser = server.manager.getUsers().get(to);
            if (remoteUser != null) remoteUser.send(this);
        } else {
            user.getSession().close();
            BoostedAudio.debug("KickICE");
        }
    }

    public static class Adapter implements JsonSerializer<RTCIcePacket>, JsonDeserializer<RTCIcePacket> {


        @Override
        public RTCIcePacket deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String typeRTC = object.get("type").getAsString();
            UUID from = UUID.fromString(object.get("from").getAsString());
            UUID to = UUID.fromString(object.get("to").getAsString());
            String candidate = BoostedAudio.getGson().toJson(object.get("candidate").getAsJsonObject());
            return new RTCIcePacket(typeRTC, candidate, from, to);
        }

        @Override
        public JsonElement serialize(RTCIcePacket rtcIcePacket, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            object.addProperty("type", rtcIcePacket.type);
            object.addProperty("from", rtcIcePacket.from.toString());
            object.addProperty("to", rtcIcePacket.to.toString());
            object.add("candidate", JsonParser.parseString(rtcIcePacket.candidate).getAsJsonObject());
            return object;
        }


    }

}