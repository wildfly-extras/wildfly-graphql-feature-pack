package org.wildfly.extras.graphql.test.client.jaxrs;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public interface FunnyApi {

    @Query("random")
    Funny getRandomFunny();

}
