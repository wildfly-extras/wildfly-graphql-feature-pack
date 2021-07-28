package org.wildfly.extras.graphql.test.client.vertx.dynamic;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.core.Document;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonObject;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class DynamicClientInsideVMTestCase {

    @Deployment(name = "server", testable = false)
    public static WebArchive deploymentServer() {
        return ShrinkWrap.create(WebArchive.class, "server.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientApi.class, Dummy.class);
    }

    @Deployment(name = "client")
    public static WebArchive deploymentClient() {
        return ShrinkWrap.create(WebArchive.class, "client.war")
//                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: io.smallrye.graphql.client.vertx services\n"), "MANIFEST.MF");
    }

    @Test
    @OperateOnDeployment("client")
    public void testTwoQueriesInOneOperationSync() throws ExecutionException, InterruptedException {
        try (VertxDynamicGraphQLClient client = (VertxDynamicGraphQLClient) DynamicGraphQLClientBuilder.newBuilder()
                .url("http://localhost:8080/server/graphql")
                .build()) {
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
    }

}
