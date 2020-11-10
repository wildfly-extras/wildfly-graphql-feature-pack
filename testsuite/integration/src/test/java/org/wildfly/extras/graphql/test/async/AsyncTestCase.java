package org.wildfly.extras.graphql.test.async;

import io.restassured.RestAssured;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
@RunAsClient
public class AsyncTestCase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "funny.war")
                .addClasses(Funny.class, FunnyApi.class, FunnyApiImpl.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void callSyncWithTypesafeClient() {
        FunnyApi funnyApi = GraphQlClientBuilder.newBuilder().endpoint("http://localhost:8080/funny/graphql").build(FunnyApi.class);
        Funny funny = funnyApi.getRandomFunny();
        Assert.assertThat(funny.getNumber(), both(greaterThan(0)).and(lessThan(1000)));
    }

    @Test
    @Ignore // TODO: does not work!!! client doesn't seem to work correctly with async types
    public void callAsyncWithTypesafeClient() throws Exception {
        FunnyApi funnyApi = GraphQlClientBuilder.newBuilder().endpoint("http://localhost:8080/funny/graphql").build(FunnyApi.class);
        Funny funny = funnyApi.getRandomFunnyAsync().toCompletableFuture().get();
        Assert.assertThat(funny.getNumber(), both(greaterThan(1000)).and(lessThan(2000)));
    }

    @Test
    @Ignore // TODO: does not work!!! client doesn't seem to work correctly with async types
    public void callAsyncScalarWithTypesafeClient() throws Exception {
        FunnyApi funnyApi = GraphQlClientBuilder.newBuilder().endpoint("http://localhost:8080/funny/graphql").build(FunnyApi.class);
        String hello = funnyApi.getStringAsync().toCompletableFuture().get();
        Assert.assertThat(hello, equalTo("Hello"));
    }

    @Test
    public void callAsyncRaw() {
        String query = TestHelper.getPayload("query { randomAsync { number } scalarAsync }");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/funny/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.randomAsync.number", both(greaterThan(1000)).and(lessThan(2000)));
    }

    // make sure that an error in one async operation is properly propagated in a partial result
    @Test
    public void callAsyncWithError() {
        String query = TestHelper.getPayload("query { randomAsync { number } errorAsync }");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/funny/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", hasSize(1))
                .body("errors[0].path[0]", equalTo("errorAsync"))
                .body("data.randomAsync.number", both(greaterThan(1000)).and(lessThan(2000)));
    }

    @Test
    public void callSyncRaw() {
        String query = TestHelper.getPayload("query { random { number } }");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/funny/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.random.number", both(greaterThan(0)).and(lessThan(1000)));
    }


}
