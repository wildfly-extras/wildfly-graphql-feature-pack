package org.wildfly.extras.graphql.test.contextpropagation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import javax.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Testing scenarios which require CDI context propagation to work under the hood.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CDIContextPropagationTest {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(TestPojo.class, ResourceThatNeedsCdiContextPropagation.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Call a query which needs the CDI context to be propagated, because it is on a RequestScoped bean
     * and involves retrieving a batch source field (these are retrieved asynchronously).
     */
    @Test
    public void testCdiContextPropagationForBatchSources() {
        String pingRequest = TestHelper.getPayload("{\n" +
                "  pojos {\n" +
                "    duplicatedMessage\n" +
                "  }\n" +
                "}");

        RestAssured.given().when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(pingRequest)
                .post("/test/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("data.pojos.duplicatedMessage", Matchers.contains("AA", "BB"));
    }

    /**
     * Same as above, but the batch source returns a list of CompletionStages.
     */
    @Test
    public void testCdiContextPropagationForBatchSourcesAsync() {
        String pingRequest = TestHelper.getPayload("{\n" +
                "  pojos {\n" +
                "    duplicatedMessageAsync\n" +
                "  }\n" +
                "}");

        RestAssured.given().when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(pingRequest)
                .post("/test/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("data.pojos.duplicatedMessageAsync", Matchers.contains("AA", "BB"));
    }

    @GraphQLApi
    // RequestScoped to make sure we test that the CDI context is propagated into operations which involve asynchronous processing
    @RequestScoped
    public static class ResourceThatNeedsCdiContextPropagation {

        /**
         * This is to make sure that `getPojos` and `duplicatedMessage` are called on the same class instance - they
         * need to share the same CDI request context rather than create a new one for calling `duplicatedMessage`.
         */
        private volatile ResourceThatNeedsCdiContextPropagation instance;

        @Query("pojos")
        public List<TestPojo> getPojos() {
            this.instance = this;
            List<TestPojo> pojos = new ArrayList<>();
            pojos.add(new TestPojo("A"));
            pojos.add(new TestPojo("B"));
            return pojos;
        }

        /**
         * This source field duplicates the message of a TestPojo (repeats it twice).
         */
        @Name("duplicatedMessage")
        public List<String> duplicatedMessage(@Source List<TestPojo> pojos) {
            if (!this.equals(this.instance)) {
                throw new IllegalStateException("duplicatedMessage was not called on the same instance as getPojos");
            }
            return pojos.stream()
                    .map(pojo -> pojo.getMessage() + pojo.getMessage())
                    .collect(Collectors.toList());
        }

        /**
         * This source field duplicates the message of a TestPojo (repeats it twice) and does it asynchronously.
         */
        @Name("duplicatedMessageAsync")
        public CompletionStage<List<String>> duplicatedMessageAsync(@Source List<TestPojo> pojos) {
            if (!this.equals(this.instance)) {
                throw new IllegalStateException("duplicatedMessageAsync was not called on the same instance as getPojos");
            }
            return CompletableFuture.completedFuture(pojos.stream()
                    .map(pojo -> pojo.getMessage() + pojo.getMessage())
                    .collect(Collectors.toList()));
        }

    }

}
