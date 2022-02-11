package org.wildfly.extension.microprofile.graphql;

import io.smallrye.graphql.entry.http.GraphQLServerWebSocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint that exposes GraphQL over websockets.
 * Just wrap the original GraphQLServerWebSocket class from SmallRye. The reason we're not using it directly is that
 * I don't know how to make the deployer see the annotations on that class - if we try to use it directly then
 * the undertow deployer will fail. So we extend that class, re-declare its annotations and delegate everything to it.
 */
@ServerEndpoint(value = "/graphql", subprotocols = { "graphql-transport-ws", "graphql-ws" })
public class WildFlyGraphQLServerWebSocket extends GraphQLServerWebSocket {

    @OnOpen
    public void onOpen(Session session) {
        super.onOpen(session);
    }

    @OnClose
    public void onClose(Session session) {
        super.onClose(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
    }

    @OnMessage
    public void handleMessage(Session session, String message) {
        super.handleMessage(session, message);
    }


}
