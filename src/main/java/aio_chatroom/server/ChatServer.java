package aio_chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private static final int THREADPOOL_SIZE = 8;
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel server;
    private List<ClientHandler> connectedClients;
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer(int port) {
        this.port = port;
        this.connectedClients = new CopyOnWriteArrayList<>();
    }

    public ChatServer() {
        this(DEFAULT_PORT);
    }


    /**
     * 启动服务器端
     */
    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
        try {
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT));
            System.out.println("服务器启动,监听端口: " + DEFAULT_PORT);
            try {
                server.accept(null, new AcceptHandler());
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(server);
        }
    }


    private void forwardMessage(AsynchronousSocketChannel client, String fwdMsg) {
        for (ClientHandler connectedClient : connectedClients) {
            AsynchronousSocketChannel currentClient = connectedClient.getClient();
            if (!currentClient.equals(client)) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put((getClientName(currentClient) + " :" + fwdMsg).getBytes());
                buffer.flip();
                currentClient.write(buffer, null, connectedClient);

                buffer.clear();
            }
        }

    }

    private String getClientName(AsynchronousSocketChannel client) {
        int clientPort = -1;
        try {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) client.getRemoteAddress();
            clientPort = inetSocketAddress.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "客户端[" + clientPort + "]";
    }


    private String receive(ByteBuffer buffer) {

        return String.valueOf(charset.decode(buffer));
    }


    /**
     * 关闭服务器
     */
    public synchronized void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否退出
     *
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equalsIgnoreCase(msg);
    }

    private synchronized void removeClient(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
        System.out.println(getClientName(clientHandler.getClient()) + "已断开连接");
        close(clientHandler.getClient());
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (server.isOpen()) {
                server.accept(null, this);
            }
            AsynchronousSocketChannel client = result;
            if (client != null && client.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 将新用户添加到在线用户列表
                ClientHandler handler = new ClientHandler(client);
                addClient(handler);
                client.read(buffer, buffer, handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败: " + exc);
        }
    }

    private void addClient(ClientHandler handler) {
        connectedClients.add(handler);
    }

    private class ClientHandler implements CompletionHandler<Integer, Object> {
        private AsynchronousSocketChannel client;

        public AsynchronousSocketChannel getClient() {
            return client;
        }

        public ClientHandler() {
        }

        public ClientHandler(AsynchronousSocketChannel client) {
            this.client = client;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null) {
                // read
                if (result <= 0) {
                    // 客户端出错,从列表中移除
                    removeClient(this);
                } else {
                    buffer.flip();
                    String fwdMsg = receive(buffer);
                    System.out.println(getClientName(client) + ": " + fwdMsg);
                    forwardMessage(client, fwdMsg);
                    buffer.clear();

                    if (readyToQuit(fwdMsg)) {
                        removeClient(this);
                    } else {
                        client.read(buffer, buffer, this);
                    }
                }
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }
}






































