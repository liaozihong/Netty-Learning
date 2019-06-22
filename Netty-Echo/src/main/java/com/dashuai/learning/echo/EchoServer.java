package com.dashuai.learning.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Echo server
 * <p/>
 * Created in 2018.12.05
 * <p/>
 *
 * @author Liaozihong
 */
public class EchoServer {
    private int port;

    /**
     * Instantiates a new Echo server.
     *
     * @param port the port
     */
    public EchoServer(int port) {
        this.port = port;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws InterruptedException {
        new EchoServer(8001).start();
    }

    /**
     * Start.步骤
     * 创建 ServerBootstrap 实例来引导服务器并随后绑定
     * 创建并分配一个 NioEventLoopGroup 实例来处理事件的处理，如接受新的连接和读/写数据。
     * 指定本地 InetSocketAddress 给服务器绑定
     * 通过 EchoServerHandler 实例给每一个新的 Channel 初始化
     * 最后调用 ServerBootstrap.bind() 绑定服务器
     * @throws InterruptedException the interrupted exception
     */
    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group).channel(NioServerSocketChannel.class)
                    //指定Socket所选端口
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });
//            绑定的服务器;sync 等待服务器关闭
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
//            关闭 channel 和 块，直到它被关闭
            f.channel().closeFuture().sync();
        } finally {
//            关闭 EventLoopGroup，释放所有资源。
            group.shutdownGracefully().sync();            //10
        }
    }
}
