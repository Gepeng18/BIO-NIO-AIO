package webserver_tutorial.processor;

import webserver_tutorial.connector.Request;
import webserver_tutorial.connector.Response;

import java.io.IOException;

public class StaticProcessor {

    public void process(Request request, Response response) {
        try {
            response.sendStaticResource();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}