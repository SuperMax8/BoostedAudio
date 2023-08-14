package fr.supermax_8.boostedaudio.web.packets;

import com.google.gson.*;
import fr.supermax_8.boostedaudio.Main;
import fr.supermax_8.boostedaudio.web.ClientWebSocket;
import fr.supermax_8.boostedaudio.web.Packet;
import fr.supermax_8.boostedaudio.web.User;
import org.eclipse.jetty.websocket.api.Session;

import java.lang.reflect.Type;

public class RTCIcePacket implements Packet {
    private String type;
    private String candidate;

    public RTCIcePacket(String type, String candidate) {
        this.type = type;
        this.candidate = candidate;
    }

    @Override
    public void onReceive(User user) {
        socket.sendPackets(session, this);
    }

    public static class Adapter implements JsonSerializer<RTCIcePacket>, JsonDeserializer<RTCIcePacket> {


        @Override
        public RTCIcePacket deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String typeRTC = object.get("type").getAsString();
            String candidate = Main.getGson().toJson(object.get("candidate").getAsJsonObject());
            return new RTCIcePacket(typeRTC, candidate);
        }

        @Override
        public JsonElement serialize(RTCIcePacket rtcIcePacket, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            object.addProperty("type", rtcIcePacket.type);
            object.add("candidate", JsonParser.parseString(rtcIcePacket.candidate).getAsJsonObject());
            return object;
        }


    }

}