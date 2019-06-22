package com.dashuai.learning.comparison.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Plain oio server
 * <p/>
 * Created in 2018.12.05
 * <p/>
 *
 * @author Liaozihong
 */
public class PlainOioServer {
    /**
     * Server.
     * 上面的方式可以工作正常，但是这种阻塞模式在大连接数的情况就会有很严重的问题，如客户端连接超时，服务器响应严重延迟，性能无法扩展。
     * 为了解决这种情况，我们可以使用异步网络处理所有的并发连接，但问题在于 NIO 和 OIO 的 API 是完全不同的，所以一个用OIO开发的网络应用程序想要使用NIO重构代码几乎是重新开发。
     * @param port the port
     * @throws IOException the io exception
     */
    public void server(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);     //1
        try {
            for (;;) {
                final Socket clientSocket = socket.accept();    //2
                System.out.println("Accepted connection from " + clientSocket);
//                创建一个新的线程来处理连接。
                new Thread(new Runnable() {                        //3
                    @Override
                    public void run() {
                        OutputStream out;
                        try {
                            out = clientSocket.getOutputStream();
//                            将消息发送到连接的客户端。
                            out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8")));                            //4
                            out.flush();
//                            一旦消息被写入和刷新时就 关闭连接
                            clientSocket.close();                //5

                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        }
                    }
                }).start();                                        //6
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
