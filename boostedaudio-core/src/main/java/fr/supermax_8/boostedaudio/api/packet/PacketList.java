package fr.supermax_8.boostedaudio.api.packet;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PacketList {

    private final List<Packet> packets;
    private static final List<String> packageNameOfPackets = new ArrayList<>() {{
        add("fr.supermax_8.boostedaudio.core.websocket.packets.client");
        add("fr.supermax_8.boostedaudio.core.websocket.packets.server");
    }};

    public PacketList(List<Packet> packets) {
        this.packets = packets;
    }

    public PacketList(Packet... packets) {
        this.packets = Arrays.asList(packets);
    }

    public List<Packet> getPackets() {
        return packets;
    }

    public static List<String> getPackageNameOfPackets() {
        return packageNameOfPackets;
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
            for (String packageName : packageNameOfPackets) {
                try {
                    String fullClassName = packageName + "." + className;
                    return Class.forName(fullClassName).asSubclass(Packet.class);
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw new JsonParseException("Unable to find Packet class: " + className + " Did you use packageNameOfPackets ?");
        }

    }


}