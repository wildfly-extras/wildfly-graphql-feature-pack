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

package org.wildfly.extras.graphql.test.federation;

import io.restassured.RestAssured;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class FederationTestCase {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "federation.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsManifestResource(new StringAsset("smallrye.graphql.federation.enabled=true\n" +
                    "smallrye.graphql.schema.includeDirectives=true"),
                "microprofile-config.properties")
            .addClasses(Product.class, Prices.class);
    }

    @Test
    public void testFederationMetadataInSchema() {
        RestAssured.given()
            .get("/federation/graphql/schema.graphql")
            .then()
            .log().body()
            .assertThat()
            .body(Matchers.containsString("type _Service"))
            .body(Matchers.containsString("type Product @extends @key(fields : \"id\")"));
    }

    @Extends
    @Key(fields = @FieldSet("id"))
    public static class Product {

        @Id
        private String id;

        @Description("The price in cent")
        private Integer price;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

    }

    @GraphQLApi
    @ApplicationScoped
    public static class Prices {

        @Query
        public Product product(@Id String id) {
            return null;
        }

    }
}
