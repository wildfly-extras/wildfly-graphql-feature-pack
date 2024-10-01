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

package org.wildfly.extras.graphql.test.beanvalidation;

import io.restassured.RestAssured;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
@RunAsClient
public class BeanValidationTestCase {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "dep1.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("smallrye.graphql.validation.enabled=true"),
                        "microprofile-config.properties")
                .addClasses(ApiWithBeanValidation.class, Person.class);
    }

    @Test
    public void queryShouldPass() {
        String query = TestHelper.getPayload("query {sayHello(person: { name: \"David\" })}");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/dep1/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.sayHello", equalTo("Hello, David"));
    }

    @Test
    public void queryShouldFail() {
        String query = TestHelper.getPayload("query {sayHello(person: { name: \"Foo\" })}");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/dep1/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors[0].extensions.violations[0].message",
                        containsString("size must be between 4 and 2147483647"))
                .body("data.sayHello", nullValue());
    }

    public static class Person {

        @Size(min = 4)
        private String name;

        public Person() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @GraphQLApi
    @ApplicationScoped
    public static class ApiWithBeanValidation {

        @Query("sayHello")
        public String sayHello(@Valid BeanValidationTestCase.Person person) {
            return "Hello, " + person.getName();
        }

    }
}
