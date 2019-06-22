package com.dashuai.learning.rpc.client;

import com.dashuai.commons.utils.JasksonUtils;
import com.dashuai.learning.rpc.client.handler.ResultHandler;
import com.dashuai.learning.rpc.model.ClassInfo;
import com.dashuai.learning.rpc.model.User;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.IOException;


public class RpcApplication {

    public static void main(String[] args) throws IOException {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("com.dashuai.learning.rpc.test.ServerServiceImpl");
        classInfo.setMethodName("getAllUser");
        classInfo.setTypes(new Class[]{User.class});
        classInfo.setObjects(new Object[]{User.builder().name("傻屌").mark("真傻!").build()});

        ResultHandler resultHandler = new ResultHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast("frameEncode", new LengthFieldPrepender(4));
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast("handler", resultHandler);
                        }
                    });
            ChannelFuture future = b.connect("localhost", 8080).sync();
            future.channel().writeAndFlush(classInfo).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        System.out.println("调用服务器数据返回回来的数据:" + JasksonUtils.toJson(resultHandler.getResponse()));
    }

}
