package fr.supermax_8.boostedaudio.core.proximitychat;

import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
import fr.supermax_8.boostedaudio.core.InternalAPI;
import fr.supermax_8.boostedaudio.core.websocket.PacketList;
import fr.supermax_8.boostedaudio.core.websocket.User;
import fr.supermax_8.boostedaudio.core.websocket.packets.AddPeerPacket;
import fr.supermax_8.boostedaudio.core.websocket.packets.RemovePeerPacket;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PeerConnection {

    private static final Map<UUID, User> users = BoostedAudioHost.getInstance().getWebSocketServer().manager.getUsers();

    private final UUID id1;
    private final UUID id2;
    private final String layerId;

    public PeerConnection(UUID id1, UUID id2, String layerId) {
        this.id1 = id1;
        this.id2 = id2;
        this.layerId = layerId;
    }

    public void link() {
        User usr1 = users.get(id1);
        User usr2 = users.get(id2);
        usr1.getRemotePeers(layerId).add(id2);
        usr2.getRemotePeers(layerId).add(id1);

        AddPeerPacket peerPacket = new AddPeerPacket(layerId, new AddPeerPacket.RTCDescription("", "createoffer"), usr1.getPlayerId(), usr2.getPlayerId(), BoostedAudioAPI.getAPI().getInternalAPI().getUsername(usr1.getPlayerId()));

        usr2.sendPacket(peerPacket);
        BoostedAudioAPI.api.debug("Sending peer packet to " + usr2.getPlayerId() + " : " + peerPacket);
    }

    public void unLink() {
        User usr1 = users.get(id1);
        User usr2 = users.get(id2);

        if (usr1 != null) {
            usr1.getRemotePeers().get(layerId).remove(id2);
            usr1.sendPacket(new PacketList(new RemovePeerPacket(layerId, id2)));
        }
        if (usr2 != null) {
            usr2.getRemotePeers().get(layerId).remove(id1);
            usr2.sendPacket(new PacketList(new RemovePeerPacket(layerId, id1)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerConnection that = (PeerConnection) o;
        return
                (id1.equals(that.id1) && id2.equals(that.id2))
                        ||
                        (id1.equals(that.id2) && id2.equals(that.id1));
    }

    @Override
    public int hashCode() {
        String str1 = id1.toString();
        String str2 = id2.toString();
        String concatenated = str1.compareTo(str2) < 0 ? str1 + str2 : str2 + str1;

        return concatenated.hashCode();
    }

}