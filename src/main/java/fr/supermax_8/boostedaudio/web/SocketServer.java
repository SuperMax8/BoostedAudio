/*
package fr.supermax_8.boostedaudio.web;

import java.io.IOException;

public class SocketServer extends WebSocketEventListenerAdapter {

    private final AudioWebSocket audioWebSocket = new AudioWebSocket();

    public void onConnect(WebSocketEventListener.WebSocketEvent<?> event) {
        audioWebSocket.onOpen(new Session(event.webSocket()));
    }


    public void onMessage(WebSocketEventListener.WebSocketEvent<?> event) {
        try {
            audioWebSocket.onMessage((String) event.message(), new Session(event.webSocket()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onClose(WebSocketEventListener.WebSocketEvent<?> event) {
        audioWebSocket.onClose(new Session(event.webSocket()));
    }

}*/
