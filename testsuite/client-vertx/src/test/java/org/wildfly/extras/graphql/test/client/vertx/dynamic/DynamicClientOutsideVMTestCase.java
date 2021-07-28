package org.wildfly.extras.graphql.test.client.vertx.dynamic;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.core.ScalarType;
import io.smallrye.graphql.client.core.Variable;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.mutiny.Multi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonObject;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.Variable.var;
import static io.smallrye.graphql.client.core.Variable.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientOutsideVMTestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "dynamic.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientApi.class, Dummy.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) DynamicGraphQLClientBuilder.newBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @After
    public void cleanup() {
        client.close();
    }

    @Test
    public void testOneQueryInOneOperationSync() throws ExecutionException, InterruptedException {
        Document document = document(
                operation(field("simple",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeSync(document).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testTwoQueriesInOneOperationSync() throws ExecutionException, InterruptedException {
        Document document = document(
                operation(field("simple",
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

    @Test
    public void testTwoOperations() throws ExecutionException, InterruptedException {
        Document document = document(
                operation("OP1",
                        field("simple",
                                field("string"))),
                operation("OP2",
                        field("simple2",
                                field("string"))));
        JsonObject data = client.executeSync(document, "OP1").getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertNull(data.getJsonObject("simple2"));
    }

    @Test
    public void testSimpleQueryAsync() {
        Document document = document(
                operation(field("simple",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeAsync(document)
                .await().atMost(Duration.ofSeconds(30)).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testSimpleQueryWithArgument() throws ExecutionException, InterruptedException {
        Document document = document(
                operation(field("queryWithArgument",
                                args(arg("number", 12)),
                                field("integer"))));
        Response response = client.executeSync(document);
        JsonObject data = response.getData();
        assertEquals(12, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

    @Test
    public void testSimpleQueryWithVariable() throws ExecutionException, InterruptedException {
        Variable number = var("number", ScalarType.GQL_INT);
        Document document = document(
                operation(
                        vars(number),
                        field("queryWithArgument",
                          args(arg("number", number)),
                          field("integer"))));
        Response response = client.executeSync(document, Collections.singletonMap("number", 555));
        JsonObject data = response.getData();
        assertEquals(555, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

    @Test
    public void testSubscription() {
        System.out.println("URL = " + testingURL.toString() + "graphql");
        Document document = document(
                operation(
                        OperationType.SUBSCRIPTION,
                        field("dummies",
                                field("integer"))));
        Multi<Response> multi = client.subscription(document);
        List<Response> responses = multi.subscribe().asStream().collect(Collectors.toList());
        for(int i = 10; i < 20; i++) {
            assertEquals(i, responses.get(i-10)
                    .getData()
                    .getJsonObject("dummies")
                    .getInt("integer")
            );
        }
    }

}
