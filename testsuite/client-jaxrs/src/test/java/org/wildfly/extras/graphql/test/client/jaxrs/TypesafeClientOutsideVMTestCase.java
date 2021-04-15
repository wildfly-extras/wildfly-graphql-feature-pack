package org.wildfly.extras.graphql.test.client.jaxrs;

import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing the typesafe client running outside the WildFly VM. Using JAX-RS transport.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientOutsideVMTestCase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "funny.war")
                .addClasses(Funny.class, FunnyApi.class, FunnyApiImpl.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    @Test
    public void testCall() {
        FunnyApi funnyApi = TypesafeGraphQLClientBuilder.newBuilder().endpoint("http://localhost:8080/funny/graphql").build(FunnyApi.class);
        Funny funny = funnyApi.getRandomFunny();
        Assert.assertTrue(funny.getNumber() > 0);
        Assert.assertTrue(funny.getNumber() < 1000);
    }

}
