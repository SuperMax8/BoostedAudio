package fr.supermax_8.boostedaudio.core.proximitychat;

import java.util.List;

public class VoiceChatResult {

    private final List<LayerInfo> layers;

    public VoiceChatResult(List<LayerInfo> layers) {
        this.layers = layers;
    }

    public List<LayerInfo> getLayers() {
        return layers;
    }

    @Override
    public String toString() {
        return "VoiceChatResult{" +
                "layers=" + layers +
                '}';
    }

}