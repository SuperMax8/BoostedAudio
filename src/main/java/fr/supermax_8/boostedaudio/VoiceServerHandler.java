/*
package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.List;

public class VoiceServerHandler extends SimpleChannelInboundHandler<Object> {

    private static List<ChannelHandlerContext> clients = new ArrayList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Gayyy ?");
        if (msg instanceof FullHttpRequest) {
            // Gérer la requête HTTP ici (si nécessaire)
        } else if (msg instanceof TextWebSocketFrame) {
            // Ajouter le nouveau client au salon vocal
            clients.add(ctx);
        } else if (msg instanceof BinaryWebSocketFrame) {
            // Envoyer les données audio aux autres clients du salon
            for (ChannelHandlerContext client : clients) {
                if (client != ctx) {
                    client.writeAndFlush(((BinaryWebSocketFrame) msg).content().retain());
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}*/
