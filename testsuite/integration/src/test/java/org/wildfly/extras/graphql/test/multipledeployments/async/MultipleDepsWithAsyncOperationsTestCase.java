package org.wildfly.extras.graphql.test.multipledeployments.async;

import io.restassured.RestAssured;
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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
@RunAsClient
public class MultipleDepsWithAsyncOperationsTestCase {

    @Deployment(name = "deploymentA", testable = false)
    public static WebArchive deploymentA() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "async-a.war")
                .addClasses(ApiA.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Deployment(name = "deploymentB", testable = false)
    public static WebArchive deploymentB() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "async-b.war")
                .addClasses(ApiB.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void callBothDeployments() {
        String query = TestHelper.getPayload("query { sayHello }");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/async-a/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.sayHello", Matchers.equalTo("HelloA"));
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/async-b/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.sayHello", Matchers.equalTo("HelloB"));
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/async-a/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.sayHello", Matchers.equalTo("HelloA"));
    }


}
