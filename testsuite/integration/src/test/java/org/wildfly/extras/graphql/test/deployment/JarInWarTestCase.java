package org.wildfly.extras.graphql.test.deployment;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class JarInWarTestCase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        JavaArchive modelJar = ShrinkWrap.create(JavaArchive.class, "model.jar")
                .addClass(Person.class);
        WebArchive war = ShrinkWrap.create(WebArchive.class, "jarinwartest.war")
                .addAsLibrary(modelJar)
                .addClasses(PersonApi.class, PersonApiImpl.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void test() {
        PersonApi personApi = GraphQlClientBuilder.newBuilder().endpoint("http://localhost:8080/jarinwartest/graphql").build(PersonApi.class);
        Person person = personApi.get();
        Assert.assertEquals("Dave", person.getName());
    }

}
