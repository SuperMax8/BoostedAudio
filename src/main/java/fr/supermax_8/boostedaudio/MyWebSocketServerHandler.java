/*
package org.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String message = frame.text();
        System.out.println("Received message from client: " + message);

        // Echo the message back to the client
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Server echo: " + message));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String connectMessage = "Client connected!";
        System.out.println(connectMessage);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(connectMessage));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String disconnectMessage = "Client disconnected!";
        System.out.println(disconnectMessage);
    }


}*/
