package aio_chatroom.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChatClient {
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private AsynchronousSocketChannel client;

    /**
     * 释放资源
     */
    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                System.out.println("关闭socket");
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            client = AsynchronousSocketChannel.open();
            Future<Void> future = client.connect(new InetSocketAddress(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT));
            future.get();

            if (client.isOpen()) {
                new Thread(() -> {
                    new UserInputHandler(this).handle();
                }).start();
            }
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                //启动异步读操作
                Future<Integer> readResult = client.read(buffer);
                int result = readResult.get();
                if (result <= 0) {
                    System.out.println("服务器断开");
                    close(client);
                    System.exit(1);
                } else {
                    buffer.flip();
                    String msg = String.valueOf(Charset.forName("UTF-8").decode(buffer));
                    buffer.clear();
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(client);
        }
    }


    public void send(String input) {
        if (input.isEmpty()) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(input.getBytes());
        Future<Integer> writeResult = client.write(buffer);
        try {
            writeResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

}
