package com.dashuai.learning.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Echo client
 * <p/>
 * Created in 2018.12.05
 * <p/>
 *
 * @author Liaozihong
 */
public class EchoClient {
    private int port;
    private String host;

    /**
     * Instantiates a new Echo server.
     *
     * @param host the host
     * @param port the port
     */
    public EchoClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws InterruptedException {
        new EchoClient("localhost", 8001).start();
    }

    /**
     * Start.步骤
     * 一个 Bootstrap 被创建来初始化客户端
     * 一个 NioEventLoopGroup 实例被分配给处理该事件的处理，这包括创建新的连接和处理入站和出站数据
     * 一个 InetSocketAddress 为连接到服务器而创建
     * 一个 EchoClientHandler 将被安装在 pipeline 当连接完成时
     * 之后 Bootstrap.connect（）被调用连接到远程的 - 本例就是 echo(回声)服务器。
     *
     * @throws InterruptedException the interrupted exception
     */
    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    //指定Socket所选端口
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            //连接到远程;等待连接完成
            ChannelFuture f = b.connect().sync();
            //当该监听器被通知连接已经建立的时候，要检查对应的状态。
            //如果该操作是成功的，那么将数据写到该 Channel。否则，要从 ChannelFuture 中检索 对应的 Throwable。
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future)  {
                    if (future.isSuccess()) {
//                        创建 ByteBuf 保存写的数据
                        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello!", Charset.defaultCharset());
//                        写数据，并刷新
                        future.channel().writeAndFlush(byteBuf);
                    } else {
                        future.cause().printStackTrace();
                    }
                }
            });
//            阻塞直到 Channel 关闭
            f.channel().closeFuture().sync();
        } finally {
//            关闭 EventLoopGroup，释放所有资源。
            group.shutdownGracefully().sync();            //10
        }
    }
}
