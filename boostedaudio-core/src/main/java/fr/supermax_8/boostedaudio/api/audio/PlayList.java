package fr.supermax_8.boostedaudio.api.audio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class PlayList {

    @Getter
    @Nullable
    @Expose
    private String id;
    @Expose
    private final CopyOnWriteArrayList<String> links;

    public PlayList(List<String> list) {
        this(null, list);
    }

    public PlayList(String id, List<String> list) {
        this.id = id;
        links = new CopyOnWriteArrayList<>(list);
    }

    public List<String> getLinks() {
        return links;
    }

}