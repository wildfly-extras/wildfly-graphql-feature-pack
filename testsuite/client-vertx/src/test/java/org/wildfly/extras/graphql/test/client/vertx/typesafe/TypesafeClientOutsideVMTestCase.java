package org.wildfly.extras.graphql.test.client.vertx.typesafe;

import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.Duration;

/**
 * Testing the typesafe client running outside the WildFly VM. Using JAX-RS transport.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientOutsideVMTestCase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "funny.war")
            .addClasses(Funny.class, FunnyApiImpl.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void testCall() {
        FunnyApi funnyApi = TypesafeGraphQLClientBuilder.newBuilder().endpoint("http://localhost:8080/funny/graphql").build(FunnyApi.class);
        Funny funny = funnyApi.getRandomFunny();
        Assert.assertTrue(funny.getNumber() > 0);
        Assert.assertTrue(funny.getNumber() < 1000);
    }

    @Test
    public void testSubscription_graphqlWSProtocol() throws IOException {
        try (FunnyApi funnyApi = TypesafeGraphQLClientBuilder.newBuilder()
            .endpoint("http://localhost:8080/funny/graphql")
            .subprotocols(WebsocketSubprotocol.GRAPHQL_WS)
            .build(FunnyApi.class)) {
            AssertSubscriber<Integer> subscriber = new AssertSubscriber<>(5);
            funnyApi.count(0, 5).subscribe(subscriber);
            subscriber.awaitCompletion(Duration.ofSeconds(10));
            subscriber.assertCompleted();
            subscriber.assertItems(0, 1, 2, 3, 4);
        }
    }

    @Test
    public void testSubscription_graphqlTransportWSProtocol() throws IOException {
        try (FunnyApi funnyApi = TypesafeGraphQLClientBuilder.newBuilder()
            .endpoint("http://localhost:8080/funny/graphql")
            .subprotocols(WebsocketSubprotocol.GRAPHQL_TRANSPORT_WS)
            .build(FunnyApi.class)) {
            AssertSubscriber<Integer> subscriber = new AssertSubscriber<>(5);
            funnyApi.count(0, 5).subscribe(subscriber);
            subscriber.awaitCompletion(Duration.ofSeconds(10));
            subscriber.assertCompleted();
            subscriber.assertItems(0, 1, 2, 3, 4);
        }
    }

}
