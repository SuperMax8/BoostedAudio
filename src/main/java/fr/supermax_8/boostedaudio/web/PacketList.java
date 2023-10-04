package fr.supermax_8.boostedaudio.web;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PacketList {

    private final List<Packet> packets;

    public PacketList(List<Packet> packets) {
        this.packets = packets;
    }

    public PacketList(Packet... packets) {
        this.packets = Arrays.asList(packets);
    }

    public PacketList() {
        this.packets = new LinkedList<>();
    }

    public List<Packet> getPackets() {
        return packets;
    }


    public static class Adapter implements JsonSerializer<PacketList>, JsonDeserializer<PacketList> {

        @Override
        public PacketList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<Packet> packets = new ArrayList<>();
            json.getAsJsonArray().forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String type = jsonObject.get("type").getAsString();
                JsonElement valueElement = jsonObject.get("value");
                Class<? extends Packet> packetClass = getPacketClass(type);
                Packet packet = context.deserialize(valueElement, packetClass);
                packets.add(packet);
            });

            return new PacketList(packets);
        }

        @Override
        public JsonElement serialize(PacketList src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (Packet packet : src.getPackets()) {
                JsonObject packetObject = new JsonObject();
                String packetName = packet.getClass().getSimpleName();
                packetObject.addProperty("type", packetName);
                JsonElement valueElement = context.serialize(packet);
                packetObject.add("value", valueElement);
                array.add(packetObject);
            }
            return array;
        }

        private Class<? extends Packet> getPacketClass(String className) {
            try {
                String fullClassName = "fr.supermax_8.boostedaudio.web.packets." + className;
                return Class.forName(fullClassName).asSubclass(Packet.class);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unable to find Packet class: " + className);
            }
        }

    }


}