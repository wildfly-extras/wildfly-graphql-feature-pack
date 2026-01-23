package org.wildfly.extension.microprofile.graphql;

import io.smallrye.graphql.servlet.GraphQLServerWebSocket;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

/**
 * Wrapper for smallrye-graphql's GraphQLServerWebSocket to set the TCCL correctly to the application class loader while handling messages.
 */
public class WildFlyGraphQLServerWebSocket extends Endpoint {

    private final GraphQLServerWebSocket delegate;
    private final ClassLoader applicationClassLoader;

    public WildFlyGraphQLServerWebSocket(GraphQLServerWebSocket delegate, ClassLoader applicationClassLoader) {
        super();
        this.delegate = delegate;
        this.applicationClassLoader = applicationClassLoader;
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        delegate.onClose(session);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        delegate.onError(session, thr);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        delegate.onOpen(session);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(applicationClassLoader);
                try {
                    delegate.handleMessage(session, message);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        });
    }

}
