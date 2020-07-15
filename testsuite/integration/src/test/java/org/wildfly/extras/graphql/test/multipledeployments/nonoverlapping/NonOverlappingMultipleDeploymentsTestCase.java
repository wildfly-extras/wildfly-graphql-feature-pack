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

package org.wildfly.extras.graphql.test.multipledeployments.nonoverlapping;

import io.restassured.RestAssured;
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
import static org.hamcrest.Matchers.equalTo;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

/**
 * Try running two deployments at once. In this case, they don't have any overlap in terms of Java package names.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class NonOverlappingMultipleDeploymentsTestCase {

    @Deployment(name = "deployment1")
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "deployment1.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage("org.wildfly.extras.graphql.test.multipledeployments.nonoverlapping.dep1");
    }

    @Deployment(name = "deployment2")
    public static WebArchive deployment2() {
        return ShrinkWrap.create(WebArchive.class, "deployment2.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage("org.wildfly.extras.graphql.test.multipledeployments.nonoverlapping.dep2");
    }

    @Test
    public void tryInvokingBothDeployments() {
        String query = TestHelper.getPayload("{ person { age } }");
        // in deployment1, the age should be 16, in deployment2 it should be 25
        // so this also verifies that deployments can clash in terms of query names
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/deployment1/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.person.age", equalTo(16));
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/deployment2/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.person.age", equalTo(25));
    }
}
