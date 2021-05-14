package org.wildfly.extras.graphql.test.subscriptions;

import javax.json.Json;
import javax.json.JsonReader;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class MyClientEndpoint {

    static final CountDownLatch CLOSED = new CountDownLatch(1);

    static final List<Integer> RECEIVED_NUMBERS = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
    }

    @OnMessage
    public void processMessage(String message) {
        System.out.println("Received message in client: " + message);
        JsonReader parser = Json.createReader(new StringReader(message));
        int receivedNumber = parser.readObject().getJsonObject("data").getInt("counting");
        RECEIVED_NUMBERS.add(receivedNumber);
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void close() {
        CLOSED.countDown();
    }

}
