package org.wildfly.extras.graphql.test.client.vertx.dynamic;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.vertx.core.MultiMap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class NamedDynamicClientInjectionTestCase {

    @Deployment(name = "server", testable = false)
    public static WebArchive deploymentServer() {
        return ShrinkWrap.create(WebArchive.class, "server.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientApi.class, Dummy.class);
    }

    @Deployment(name = "client")
    public static WebArchive deploymentClient() {
        return ShrinkWrap.create(WebArchive.class, "client.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset(
                        "dummy/mp-graphql/url=http://localhost:8080/server/graphql\n" +
                                "dummy/mp-graphql/header/My-Custom-Header=Header-Value"),
                        "microprofile-config.properties")
                // FIXME: how to avoid requiring the deployment to add 'io.smallrye.graphql.client.common meta-inf' explicitly here?
                // io.vertx.core is needed because we use MultiMap in the test code
                .addAsManifestResource(new StringAsset("Dependencies: io.vertx.core, io.smallrye.graphql.client.vertx services, io.smallrye.graphql.client.common meta-inf\n"), "MANIFEST.MF");
    }

    @Inject
    @GraphQLClient("dummy")
    DynamicGraphQLClient client;

    @Test
    @OperateOnDeployment("client")
    public void testInjectedClient() throws ExecutionException, InterruptedException {
        Document document = document(
                operation("SimpleQuery",
                        field("simple",
                                field("string"),
                                field("integer")),
                        field("simple2",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeSync(document).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
        assertEquals("asdfgh", data.getJsonObject("simple2").getString("string"));
        assertEquals(31, data.getJsonObject("simple2").getInt("integer"));
    }

    // check that the injected client instance passes the HTTP header that was requested in the configuration
    // we don't actually call the server side here, just inspect the internals of the client instance
    @Test
    @OperateOnDeployment("client")
    public void verifyHttpHeaders() throws NoSuchFieldException, IllegalAccessException {
        Field field = VertxDynamicGraphQLClient.class.getDeclaredField("headers");
        field.setAccessible(true);
        MultiMap headers = (MultiMap)field.get(client);
        Assert.assertEquals("Header-Value", headers.get("My-Custom-Header"));
    }

}
