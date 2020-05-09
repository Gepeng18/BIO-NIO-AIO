package webserver_tutorial;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args)throws Exception{
        Socket socket = new Socket("localhost", 8888);
        OutputStream output = socket.getOutputStream();
        output.write("GET /servlet/TimeServlet HTTP/1.1".getBytes());
        socket.shutdownOutput();

        InputStream input = socket.getInputStream();
        byte[] buffer = new byte[2048];
        int length = input.read(buffer);
        StringBuilder response = new StringBuilder();
        for (int j=0; j<length; j++) {
            response.append((char)buffer[j]);
        }
        System.out.println(response.toString());
        socket.shutdownInput();

        socket.close();
    }
}
