package org.wildfly.extras.graphql.test.mutiny;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.graphql.api.Context;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.wildfly.extras.graphql.test.TestHelper.getPayload;

@RunWith(Arquillian.class)
@RunAsClient
public class MutinyTest {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "mutiny-test.war")
                .addClasses(MutinyResource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testUni() {
        String pingRequest = getPayload("{ uni }");

        RestAssured.given().when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(pingRequest)
                .post("/mutiny-test/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("data.uni", equalTo("OK"));

    }

    @Test
    public void testUniConcurrently() throws ExecutionException, InterruptedException {
        final int iterations = 50;
        String pingRequest = getPayload("{ uni }");
        CompletableFuture<String>[] futures = new CompletableFuture[iterations];
        for(int i = 0; i < 50; i++) {
            futures[i] = CompletableFuture.supplyAsync(() -> {
                return RestAssured.given().when()
                        .accept(ContentType.JSON)
                        .contentType(ContentType.JSON)
                        .body(pingRequest)
                        .post("/mutiny-test/graphql")
                        .then()
                        .assertThat()
                        .statusCode(200)
                        .and()
                        .extract().body().jsonPath().getString("data.uni");
            });
        }
        CompletableFuture.allOf(futures).get();
        for(CompletableFuture<String> future : futures) {
            Assert.assertEquals("OK", future.get());
        }
    }

    @GraphQLApi
    public static class MutinyResource {

        // list of already seen execution ids - we use this to verify that each query gets its correct Context
        // with its unique ID
        static List<String> executionIds = new CopyOnWriteArrayList<>();

        @Query
        public Uni<String> uni(Context context) {
            String executionId = context.getExecutionId();
            assertNotNull(executionId);
            assertFalse(executionIds.contains(executionId));
            assertEquals("uni", context.getFieldName());

            return Uni.createFrom().item(() -> {
                // makes sure that if called multiple times at once, it is indeed executed concurrently
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // makes sure the context was propagated correctly
                assertEquals(executionId, context.getExecutionId());
                assertEquals("uni", context.getFieldName());
                return "OK";
            });
        }

    }

}
