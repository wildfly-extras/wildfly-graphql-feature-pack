package org.wildfly.extension.microprofile.graphql;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphqlErrorBuilder;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.enterprise.context.control.RequestContextController;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicReference;

@ServerEndpoint("/graphql")
public class SubscriptionWebSocket {

    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    @Inject
    ExecutionService executionService;

    @Inject
    GraphQLConfig config;

    @Inject
    RequestContextController requestContextController;

    @OnClose
    public void onClose(Session session) {
        Subscription subscription = subscriptionRef.get();
        if(subscription != null) {
            subscription.cancel();
        }
        subscriptionRef.set(null);
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        throwable.printStackTrace();
        if(session.isOpen()) {
            session.close();
        }
    }

    @OnMessage
    public void handleMessage(Session session, String message) {
        requestContextController.activate();
        try {
            try (JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(message))) {
                JsonObject jsonInput = jsonReader.readObject();

                ExecutionResponse executionResponse = executionService.execute(jsonInput);

                Publisher<ExecutionResult> stream = executionResponse.getExecutionResult().getData();

                if (stream != null) { // TODO: How to handle when null ?
                    stream.subscribe(new Subscriber<ExecutionResult>() {

                        @Override
                        public void onSubscribe(Subscription s) {
                            subscriptionRef.set(s);
                            request(1, session);
                        }

                        @Override
                        public void onNext(ExecutionResult er) {
                            try {
                                if (session.isOpen()) {
                                    ExecutionResponse executionResponse = new ExecutionResponse(er, config);
                                    session.getBasicRemote().sendText(executionResponse.getExecutionResultAsString());
                                }
                            }
                            catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            request(1, session);
                        }

                        @Override
                        public void onError(Throwable t) {
                            try {
                                // TODO: Below must move to SmallRye, and once 1.2.1 is releaes we can remove it here
                                //       Once in SmallRye, this will also follow the propper error rule (show/hide) and add more details.
                                ExecutionResultImpl result = new ExecutionResultImpl(GraphqlErrorBuilder.newError()
                                        .message(t.getMessage())
                                        .build());
                                ExecutionResponse response = new ExecutionResponse(result, config);
                                session.getBasicRemote().sendText(response.getExecutionResultAsString());
                            }
                            catch (IOException ex) {
                                ex.printStackTrace();
                            } finally {
                                try {
                                    session.close();
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onComplete() {
                            try {
                                session.close();
                            }
                            catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
        } finally {
            requestContextController.deactivate();
        }

    }

    private void request(int n, Session session) {
        Subscription subscription = subscriptionRef.get();
        if (subscription != null && session.isOpen()) {
            subscription.request(n);
        }
    }

}
