package com.ethan.server;

import com.ethan.codec.Spliter;
import com.ethan.codec.PacketCodecHandler;
import com.ethan.server.handle.AuthHandler;
import com.ethan.server.handle.IMHandler;
import com.ethan.server.handle.LoginRequestHandler;
import com.ethan.server.handle.idle.HeartBeatRequestHandler;
import com.ethan.idle.IMIdleStateHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


public class NettyServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                        System.out.println("服务端启动中...'");
                    }
                })
                // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                // TCP底层心跳机制，true为开启
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // Nagle算法，true表示关闭，false表示开启
                .childOption(ChannelOption.TCP_NODELAY, true)
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        System.out.println("initChannel():--> child worker启动..." );
                        // channel.pipeline().addLast(new LifeCyCleTestHandler());
                        // 空闲检测
                        channel.pipeline().addLast(new IMIdleStateHandler());

                        channel.pipeline().addLast(new Spliter());
                        // channel.pipeline().addLast(new FirstServerHandler());
                        // channel.pipeline().addLast(new PacketDecoder());
                        channel.pipeline().addLast(PacketCodecHandler.INSTANCE);

                        // channel.pipeline().addLast(new LoginRequestHandler());
                        channel.pipeline().addLast(LoginRequestHandler.INSTANCE);
                        // check heart beat
                        channel.pipeline().addLast(HeartBeatRequestHandler.INSTANCE);
                        channel.pipeline().addLast(AuthHandler.INSTANCE);

                        channel.pipeline().addLast(IMHandler.INSTANCE);
                        /*channel.pipeline().addLast(MessageRequestHandler.INSTANCE);
                        channel.pipeline().addLast(CreateGroupRequestHandler.INSTANCE);
                        channel.pipeline().addLast(JoinGroupRequestHandler.INSTANCE);
                        channel.pipeline().addLast(QuitGroupRequestHandler.INSTANCE);
                        channel.pipeline().addLast(ListGroupMembersRequestHandler.INSTANCE);
                        channel.pipeline().addLast(GroupMessageRequestHandler.INSTANCE);*/

                        // channel.pipeline().addLast(new PacketEncoder());

                        // -------------------------------------package------------------------------------------------------
                        // channel.pipeline().addLast(new InBoundHandlerStaticProxy(new InBoundHandlerA()));
                        // channel.pipeline().addLast(new InBoundHandlerStaticProxy(new InBoundHandlerB()));
                        // channel.pipeline().addLast(new InBoundHandlerStaticProxy(new InBoundHandlerC()));
                        // channel.pipeline().addLast(InboundProxyGenerator.newInstance(new InBoundHandlerA()));
                        // channel.pipeline().addLast(InboundProxyGenerator.newInstance(new InBoundHandlerB()));
                        // channel.pipeline().addLast(InboundProxyGenerator.newInstance(new InBoundHandlerC()));
                        // channel.pipeline().addLast(new ServerLoginHandler());

                        // channel.pipeline().addLast(new OutBoundHandlerStaticProxy(new OutBoundHandlerA()));
                        // channel.pipeline().addLast(new OutBoundHandlerStaticProxy(new OutBoundHandlerB()));
                        // channel.pipeline().addLast(new OutBoundHandlerStaticProxy(new OutBoundHandlerC()));
                        // channel.pipeline().addLast(OutboundProxyGenerator.newInstance(new OutBoundHandlerA()));
                        // channel.pipeline().addLast(new OutBoundHandlerB());
                        // channel.pipeline().addLast(new OutBoundHandlerC());
                    }
                });
        bind(serverBootstrap, 8080);

    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("端口[" + port + "]绑定成功!");
                } else {
                    System.out.println("端口[" + port + "]绑定失败");
                    bind(serverBootstrap, port + 1);
                }
            }
        });
    }
}
