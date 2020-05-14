package aio_chatroom.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.AsynchronousSocketChannel;


public class UserInputHandler {
    private ChatClient client;

    public UserInputHandler(ChatClient client) {
        this.client = client;
    }

    public void handle() {
        try {
            // 等待用户输入消息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();
                client.send(input);
                if ("quit".equalsIgnoreCase(input)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
