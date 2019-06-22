package com.dashuai.learning.comparison.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Netty oio server
 * <p/>
 * Created in 2018.12.05
 * <p/>
 *
 * @author Liaozihong
 */
public class NettyNioServer {
    /**
     * Server.
     *
     * @param port the port
     * @throws Exception the exception
     */
    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
//            创建一个 ServerBootstrap
            ServerBootstrap b = new ServerBootstrap();
//            使用 NioEventLoopGroup 允许非阻塞模式（NIO）
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
//            指定 ChannelInitializer 将给每个接受的连接调用
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
//                            添加的 ChannelHandler 拦截事件，并允许他们作出反应
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                    写信息到客户端，并添加 ChannelFutureListener 当一旦消息写入就关闭连接
                                    ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
//            绑定服务器来接受连接
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            //释放全部资源
            bossGroup.shutdownGracefully().sync();                    //7
            workerGroup.shutdownGracefully().sync();
        }
    }
}
