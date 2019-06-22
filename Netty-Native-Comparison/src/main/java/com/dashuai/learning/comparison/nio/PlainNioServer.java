package com.dashuai.learning.comparison.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Plain nio server
 * <p/>
 * Created in 2018.12.05
 * <p/>
 *
 * @author Liaozihong
 */
public class PlainNioServer {
    /**
     * Server.
     *
     * @param port the port
     * @throws IOException the io exception
     */
    public static void server(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        //绑定服务器到指定端口
        ss.bind(address);
//        .打开 selector 处理 channel
        Selector selector = Selector.open();
//        注册 ServerSocket 到 ServerSocket ，并指定这是专门接受新连接。
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (; ; ) {
            try {
//                等待新的事件来处理。这将阻塞，直到一个事件是传入
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                // handle exception
                break;
            }
//            从收到的所有事件中 获取 SelectionKey 实例。
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
//                    检查该事件是一个新的连接准备好接受
                    if (key.isAcceptable()) {
                        ServerSocketChannel server =
                                (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
//                        接受客户端，并用 selector 进行注册。
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());
                        System.out.println(
                                "Accepted connection from " + client);
                    }
//                    检查 socket 是否准备好写数据。
                    if (key.isWritable()) {
                        SocketChannel client =
                                (SocketChannel) key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
//                            将数据写入到所连接的客户端。如果网络饱和，连接是可写的，那么这个循环将写入数据，直到该缓冲区是空的。
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
//                        关闭连接
                        client.close();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        // 在关闭时忽略
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        server(8081);
    }

}
