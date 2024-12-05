package fr.supermax_8.boostedaudio.api.audio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class PlayList {

    @Getter
    @Nullable
    @Expose
    private String id;
    @Expose
    private final CopyOnWriteArrayList<String> links;
    @Expose
    @Getter
    @Setter
    private boolean synchronous;

    public PlayList(List<String> list, boolean synchronous) {
        this(null, list, synchronous);
    }

    public PlayList(@Nullable String id, List<String> list, boolean synchronous) {
        this.id = id;
        links = new CopyOnWriteArrayList<>(list);
        this.synchronous = synchronous;
    }

    public List<String> getLinks() {
        return links;
    }

}