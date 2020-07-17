/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extras.graphql.test.ui;

import io.restassured.RestAssured;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.containsString;

/**
 * Verify that when an application contains the GraphiQL jar, it will be exposed and working after the deployment.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class GraphQLUITestCase {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "my-awesome-deployment.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibrary(Maven.resolver()
                        .loadPomFromFile("pom.xml")
                        .resolve("io.smallrye:smallrye-graphql-ui-graphiql")
                        .withoutTransitivity()
                        .asSingleFile())
                .addClass(DummyApi.class);
    }

    @Test
    public void verifyUiIsAvailable() {
        RestAssured.given()
                .get("/my-awesome-deployment/graphql-ui")
                .then()
                .statusCode(200);

        // check that render.js was updated with the proper curated path to the /graphql endpoint for this deployment
        RestAssured.given()
                .get("/my-awesome-deployment/graphql-ui/render.js")
                .then()
                .statusCode(200)
                .body(containsString("const api = '/my-awesome-deployment/graphql';"));
    }
}
