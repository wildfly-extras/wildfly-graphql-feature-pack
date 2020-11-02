package org.wildfly.extras.graphql.test.multipledeployments.async;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@GraphQLApi
public class ApiB {

    @Query
    public CompletionStage<String> sayHello() {
        return CompletableFuture.completedFuture("HelloB");
    }

}
