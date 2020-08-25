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

package org.wildfly.extras.graphql.test.source;

import io.restassured.RestAssured;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
@RunAsClient
public class BatchSourceTestCase {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "batch.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void queryShouldPass() {
        String query = TestHelper.getPayload("query { allPersons { name desc } }");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/batch/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.allPersons[0].name", equalTo("Joe"))
                .body("data.allPersons[0].desc", equalTo("The awesome Joe"))
                .body("data.allPersons[1].name", equalTo("Paul"))
                .body("data.allPersons[1].desc", equalTo("The awesome Paul"));
    }



    static class Person {

        private String name;

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @GraphQLApi
    public static class ApiWithBatchSource {

        @Query("allPersons")
        public List<Person> allPeople() {
            List<Person> ret = new ArrayList<>();
            ret.add(new Person("Joe"));
            ret.add(new Person("Paul"));
            return ret;
        }

        @Name("desc")
        public List<String> sayHello(@Source List<Person> people) {
            return people.stream()
                    .map(person -> "The awesome " + person.getName())
                    .collect(Collectors.toList());
        }

    }

}
