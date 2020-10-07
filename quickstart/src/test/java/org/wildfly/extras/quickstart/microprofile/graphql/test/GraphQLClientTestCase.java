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

package org.wildfly.extras.quickstart.microprofile.graphql.test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.quickstart.microprofile.graphql.Film;
import org.wildfly.extras.quickstart.microprofile.graphql.Hero;
import org.wildfly.extras.quickstart.microprofile.graphql.LightSaber;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class GraphQLClientTestCase {

    @Deployment(name = "server", testable = false)
    public static WebArchive serverDeployment() {
        return ShrinkWrap.create(WebArchive.class, "server.war")
                .addPackage(Film.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(name = "client")
    public static WebArchive clientDeployment() {
        return ShrinkWrap.create(WebArchive.class, "client.war")
                // only include the model classes plus the client-side API interface, not the GraphQL API implementation
                .addClasses(Film.class, Hero.class, LightSaber.class, GalaxyClientApi.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                // depend on the client libraries included in the feature pack as static modules
                .addAsManifestResource(new StringAsset("Dependencies: io.smallrye.graphql.client services\n"), "MANIFEST.MF");
    }

    @Test
    @OperateOnDeployment("client")
    public void testGetAllFilms() {
        GalaxyClientApi client = GraphQlClientBuilder.newBuilder()
                .endpoint("http://localhost:8080/server/graphql")
                .build(GalaxyClientApi.class);

        List<Film> allFilms = client.getAllFilms();

        Film aNewHope = allFilms.get(0);
        assertEquals("A New Hope", aNewHope.getTitle());

        Film theEmpireStrikesBack = allFilms.get(1);
        assertEquals("The Empire Strikes Back", theEmpireStrikesBack.getTitle());

        Film returnOfTheJedi = allFilms.get(2);
        assertEquals("Return Of The Jedi", returnOfTheJedi.getTitle());
    }
}
