package fr.supermax_8.boostedaudio.core.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.api.event.EventManager;
import fr.supermax_8.boostedaudio.api.event.events.UserQuitEvent;
import fr.supermax_8.boostedaudio.api.packet.Packet;
import fr.supermax_8.boostedaudio.api.packet.PacketList;
import fr.supermax_8.boostedaudio.core.proximitychat.PeerConnection;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerPacketListener;
import fr.supermax_8.boostedaudio.core.serverpacket.ServerUser;
import fr.supermax_8.boostedaudio.core.websocket.packets.TrustPacket;
import lombok.Getter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class AudioWebSocketServer extends WebSocketServer {

    @Getter
    private static AudioWebSocketServer instance;

    public final ConnectionManager manager = new ConnectionManager();
    private boolean isOpen = false;
    private static final Gson gson = BoostedAudioAPI.api.getGson();
    public static BiFunction<String, WebSocket, Boolean> serverProxyCheck;
    public static BiConsumer<String, ServerUser> proxyConsumer;

    public AudioWebSocketServer(InetSocketAddress address) {
        super(address);
        instance = this;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            manager.sessionUsers.put(webSocket, Optional.empty());
            BoostedAudioAPI.api.debug("New connection WebSocket : " + webSocket.getRemoteSocketAddress().getAddress() + " / " + manager.getSessionUsers().size());
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        try {
            BoostedAudioAPI.api.debug("WebSocket connection closed : " + webSocket.getRemoteSocketAddress().getAddress());
            BoostedAudioAPI.api.debug("REASON " + s + " STATUSCODE: " + i);

            Optional<Object> user = manager.getSessionUsers().remove(webSocket);
            BoostedAudioAPI.api.debug("SessionUsersSize " + manager.getSessionUsers().size());
            if (user.isEmpty()) return;

            Object obj = user.get();
            if (obj instanceof HostUser hostUser) {
                UUID playerId = hostUser.getPlayerId();
                HostUser realUser = (HostUser) manager.getUsers().remove(playerId);

                if (realUser == null) return;

                for (Map.Entry<String, Set<UUID>> entry : realUser.getRemotePeers().entrySet()) {
                    String layerId = entry.getKey();

                    for (UUID id : entry.getValue()) {
                        HostUser usr = (HostUser) manager.getUsers().get(id);
                        if (usr == null) continue;
                        new PeerConnection(realUser.getPlayerId(), usr.getPlayerId(), layerId).unLink();
                    }
                }

                UserQuitEvent userQuitEvent = new UserQuitEvent(realUser);
                EventManager.getInstance().callEvent(userQuitEvent);
            } else if (obj instanceof ServerUser serverUser) {
                manager.getServers().remove(serverUser.getServerId());
            }
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket client, String message) {
        Optional<Object> user = manager.getSessionUsers().get(client);
        if (user.isPresent() && user.get() instanceof ServerUser serverUser) {
            proxyConsumer.accept(message, serverUser);
            return;
        }

        PacketList packetList;
        try {
            packetList = gson.fromJson(message, PacketList.class);
        } catch (JsonSyntaxException e) {
            if (serverProxyCheck != null && serverProxyCheck.apply(message, client)) return;
            client.close();
            BoostedAudioAPI.api.debug("ERREUR RECEIVED MESSAGE");
            BoostedAudioAPI.api.debug(message);
            return;
        } catch (Exception e) {
            if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
            BoostedAudioAPI.api.debug(message);
            return;
        }


        if (user.isPresent()) {
            Object obj = user.get();
            if (obj instanceof HostUser hostUser) {
                for (Packet packet : packetList.getPackets()) packet.onReceive(hostUser, this);
            }
        } else testToTrust(client, packetList);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        BoostedAudioAPI.api.debug("ERRRRREEEEUR WEBSOCKET " + webSocket.getRemoteSocketAddress());
        BoostedAudioAPI.api.debug(e.toString());
        if (BoostedAudioAPI.api.getConfiguration().isDebugMode()) e.printStackTrace();
    }

    @Override
    public void onStart() {
        BoostedAudioAPI.api.debug("WebSocketServer Open");
        isOpen = true;
        setConnectionLostTimeout(10);
    }

    private void testToTrust(WebSocket session, PacketList message) {
        Packet packet;
        if (message.getPackets().isEmpty() || !((packet = message.getPackets().get(0)) instanceof TrustPacket)) {
            BoostedAudioAPI.api.debug("Kick Untrust: " + session);
            session.close();
        } else packet.onReceive(new HostUser(session, null, null), this);
    }


    public boolean isOpen() {
        return isOpen;
    }


}