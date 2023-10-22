//package fr.supermax_8.boostedaudio.core.ingame;
//
//import fr.supermax_8.boostedaudio.core.BoostedAudioHost;
//import fr.supermax_8.boostedaudio.core.utils.AroundManager;
//import fr.supermax_8.boostedaudio.core.websocket.Audio;
//import fr.supermax_8.boostedaudio.core.websocket.User;
//import org.bukkit.Location;
//
//import java.util.concurrent.ConcurrentHashMap;
//
//public class SpeakerManager {
//
//    private final AroundManager manager = BoostedAudioHost.getInstance().getAroundManager();
//    public final ConcurrentHashMap<Location, Audio> speakers = new ConcurrentHashMap<>();
//
//    public SpeakerManager() {
//    }
//
//    public void addSpeaker(Audio audio) {
//        Location location = audio.getSpatialInfo().getLocation().toBukkitLocation();
//        speakers.put(location, audio);
//        manager.addAround(location, audio.getSpatialInfo().getMaxVoiceDistance(),
//                p -> {
//                    User user = BoostedAudioHost.getInstance().getWebSocketServer().manager.getUsers().get(p.getUniqueId());
//                    if (user == null) return;
//                    user.playAudio(audio);
//                },
//                p -> {
//                    User user = BoostedAudioHost.getInstance().getWebSocketServer().manager.getUsers().get(p.getUniqueId());
//                    if (user == null) return;
//                    user.stopAudio(audio);
//                },
//                p -> BoostedAudioHost.getInstance().getWebSocketServer().manager.getUsers().containsKey(p.getUniqueId())
//        );
//    }
//
//    public void removeSpeaker(Location location) {
//        manager.removeAround(location);
//        speakers.remove(location);
//    }
//
//    public ConcurrentHashMap<Location, Audio> getSpeakers() {
//        return speakers;
//    }
//
//}