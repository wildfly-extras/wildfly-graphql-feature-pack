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

package org.wildfly.extras.graphql.test.metrics;

import io.restassured.RestAssured;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.graphql.test.TestHelper;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.wildfly.extras.graphql.test.TestHelper.MEDIATYPE_JSON;

@RunWith(Arquillian.class)
public class MetricsTestCase {

    @Deployment
    public static WebArchive deployment1() {
        return ShrinkWrap.create(WebArchive.class, "metrics.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("smallrye.graphql.metrics.enabled=true"),
                        "microprofile-config.properties")
                .addClass(DummyApi.class);
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void callEndpoint() {
        String query = TestHelper.getPayload("query {hello}");
        RestAssured.given()
                .body(query)
                .contentType(MEDIATYPE_JSON)
                .post("/metrics/graphql")
                .then()
                .log().body()
                .assertThat()
                .body("errors", nullValue())
                .body("data.hello", equalTo("hello"));
    }

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry metricRegistry;

    @Test
    @InSequence(2)
    public void verifyMetrics() {
        SimpleTimer simpleTimer = metricRegistry.getSimpleTimers().get(new MetricID("mp_graphql",
                new Tag("type", "QUERY"),
                new Tag("name", "hello"),
                new Tag("source", "false")));
        Assert.assertNotNull("Can't find expected metrics in the registry", simpleTimer);
        Assert.assertEquals(1L, simpleTimer.getCount());
    }

    @GraphQLApi
    public static class DummyApi {

        @Query
        public String hello() {
            return "hello";
        }

    }

}
