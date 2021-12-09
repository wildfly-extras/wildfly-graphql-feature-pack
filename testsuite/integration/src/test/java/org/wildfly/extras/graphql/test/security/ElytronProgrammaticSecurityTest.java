package org.wildfly.extras.graphql.test.security;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import javax.enterprise.context.RequestScoped;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
@RunAsClient
// FIXME: due to some changes in wf26 this does not work right now
// - it probably needs some more configuration on the WF side
@Ignore
public class ElytronProgrammaticSecurityTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "elytronsecurity.war")
                .addClasses(ProgrammaticallySecuredApi.class)
                .addAsWebInfResource(new StringAsset("<jboss-web><security-domain>other</security-domain></jboss-web>"),
                        "jboss-web.xml")
                .addAsWebInfResource(new StringAsset("<web-app></web-app>"), "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    // recreate this with curl:
    /*
        curl -H"Authorization: Basic am9lOmpvZUlzQXdlc29tZTIwMTMl" \
           -X POST localhost:8080/_DEFAULT___DEFAULT__elytronsecurity/graphql \
           -d '{"query": "{whoAmI}"}'
     */
    @Test
    public void testAuthorized() {
        String query = TestHelper.getPayload("query { whoAmI }");
        RestAssured.given()
                .header(new Header("Authorization", "Basic am9lOmpvZUlzQXdlc29tZTIwMTMl")) // joe:joeIsAwesome2013%
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/elytronsecurity/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.whoAmI", Matchers.equalTo("joe"));
    }

    @Test
    public void testWrongPassword() {
        String query = TestHelper.getPayload("query { whoAmI }");
        RestAssured.given()
                .header(new Header("Authorization", "Basic am9lOmpwZUlzQXdlc29tZTIwMTMl")) // incorrect credentials
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/elytronsecurity/graphql")
                .then()
                .log().body()
                .assertThat()
                .statusCode(401);
    }

    @GraphQLApi
    @RequestScoped
    public static class ProgrammaticallySecuredApi {

        @Query
        public String whoAmI() {
            org.wildfly.security.auth.server.SecurityIdentity identity
                    = org.wildfly.security.auth.server.SecurityDomain.getCurrent()
                    .getCurrentSecurityIdentity();
            return identity.getPrincipal().getName();
        }

    }
}
