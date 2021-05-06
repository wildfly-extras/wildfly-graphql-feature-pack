package org.wildfly.extras.graphql.test.subscriptions;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Test for a simple subscription operation implemented by a org.reactivestreams.Publisher
 * and used by a pure websocket client.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PublisherTest {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "ROOT.war")
                .addClasses(PublisherResource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void testPublisher() throws DeploymentException, IOException, InterruptedException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/graphql";
        System.out.println("Connecting to " + uri);
        Session session = container.connectToServer(MyClientEndpoint.class, URI.create(uri));
        session.getBasicRemote().sendText("{\"query\":\"subscription a { counting }\",\"variables\":null,\"operationName\":\"a\"}");
        Assert.assertTrue("Timeout waiting for the websocket to close!",
                MyClientEndpoint.CLOSED.await(10, TimeUnit.SECONDS));
        for(int i = 0; i < 10; i++) {
            Assert.assertTrue(MyClientEndpoint.RECEIVED_NUMBERS.contains(i));
        }
        session.close();
    }


}
